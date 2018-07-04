package org.vaadin.addons.vaactor.chat

import org.vaadin.addons.vaactor._
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.listbox.ListBox
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.{ HorizontalLayout, VerticalLayout }
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.{ DataProvider, ListDataProvider }

import akka.actor.Actor.Receive
import akka.actor.ActorRef

import scala.collection.JavaConverters._

object ChatComponent {

  trait Strategy {
    def login(name: String, sender: ActorRef): Unit

    def logout(name: String, sender: ActorRef): Unit

    def send(name: String, text: String, sender: ActorRef): Unit
  }

}

/** UI to be created by servlet
  *
  * @author Otto Ringhofer
  */
class ChatComponent(title: String, strategy: ChatComponent.Strategy)
  extends VerticalLayout with Vaactor.HasActor {

  /** Contains list of messages from chatroom */
  val chatList = new java.util.ArrayList[ChatServer.Statement]()
  val chatDataProvider: ListDataProvider[ChatServer.Statement] = DataProvider.ofCollection[ChatServer.Statement](chatList)
  val chatPanel: Grid[ChatServer.Statement] = new Grid[ChatServer.Statement]() {
    addColumn(d => d.name)
    addColumn(d => d.msg)
    setWidth("400px")
    setDataProvider(chatDataProvider)
  }

  /** Contains list of chatroom menbers */
  val memberList = new java.util.ArrayList[String]()
  val memberDataProvider: ListDataProvider[String] = DataProvider.ofCollection[String](memberList)
  val memberPanel: ListBox[String] = new ListBox[String] {
    setDataProvider(memberDataProvider)
  }

  /** Contains username */
  val userCaption = new Label()
  val userName = new TextField()

  val loginPanel: HorizontalLayout = new HorizontalLayout(
    userName,
    new Button("Login", _ => strategy.login(userName.getValue, self))
  )

  val logoutBtn = new Button("Logout", _ => strategy.logout(userName.getValue, self))

  val messageText = new TextField()
  val messagePanel: HorizontalLayout = new HorizontalLayout(
    messageText,
    new Button(
      "Send", _ => {
        strategy.send(userName.getValue, messageText.getValue, self)
        messageText.setValue("")
        messageText.focus()
      }
    ))

  /** Contains user interface for login/logout and sending of messages */
  val userPanel = new VerticalLayout(
    userCaption,
    new HorizontalLayout(loginPanel, logoutBtn),
    new HorizontalLayout(
      messagePanel,
      new Button("Clear", _ => {
        chatList.clear()
        chatDataProvider.refreshAll()
      }))
  )

  add(
    new Label(title),
    new HorizontalLayout(
      new VerticalLayout(userPanel, chatPanel),
      memberPanel
    )
  )

  showSubscription()

  def showSubscription(name: String = ""): Unit = {
    val success = name.nonEmpty
    if (success)
      ChatServer.chatServer ! ChatServer.RequestMembers
    else
      showMembers(Nil)
    loginPanel.setEnabled(!success)
    logoutBtn.setEnabled(success)
    messagePanel.setEnabled(success)
    userCaption.setText(if (success) s"Logged in: $name" else "Please login ...")

  }

  def showMembers(members: Seq[String]): Unit = {
    memberList.clear()
    memberList.addAll(members.asJava)
    memberDataProvider.refreshAll()
  }

  def showStatement(statement: ChatServer.Statement): Unit = {
    chatList.add(statement)
    chatDataProvider.refreshAll()
  }

  override def receive: Receive = {
    // User entered chatroom, update member list
    case ChatServer.Enter(name) =>
      Notification.show(s"$name entered the chatroom")
      ChatServer.chatServer ! ChatServer.RequestMembers
    // User left chatroom, update member list
    case ChatServer.Leave(name) =>
      Notification.show(s"$name left the chatroom")
      ChatServer.chatServer ! ChatServer.RequestMembers
    // Message from chatroom, update message list
    case statement: ChatServer.Statement =>
      showStatement(statement)
    // Member list of chatroom, update member list
    case ChatServer.Members(members) =>
      showMembers(members)
    // Subscription successful, adjust user interface state
    case ChatServer.SubscriptionSuccess(name) =>
      showSubscription(name)
    // Subscription cancelled, adjust user interface state
    case ChatServer.SubscriptionCancelled(_) =>
      showSubscription()
    // Subscription failed, show warning
    case ChatServer.SubscriptionFailure(error) =>
      Notification.show(error)
  }

}
