package org.vaadin.addons.vaactor.chat

import org.vaadin.addons.vaactor._
import org.vaadin.addons.vaactor.demo.Session
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.listbox.ListBox
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.Notification.Position
import com.vaadin.flow.component.orderedlayout.{ HorizontalLayout, VerticalLayout }
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.{ Component, Composite }
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
  extends Composite[Component] with Vaactor.HasActor with Vaactor.HasSession with Vaactor.AttachSession {

  /** Send to session actor on attach */
  override val attachMessage: Any = Session.Attached

  /** Send to session actor on detach */
  override val detachMessage: Any = Session.Detached

  /** Contains list of messages from chatroom */
  val chatList = new java.util.ArrayList[ChatServer.Statement]()
  val chatDataProvider: ListDataProvider[ChatServer.Statement] = DataProvider.ofCollection[ChatServer.Statement](chatList)
  val chatPanel: Grid[ChatServer.Statement] = new Grid[ChatServer.Statement]() {
    // todo setCaption("Chat")
    addColumn(d => d.name)
    addColumn(d => d.msg)
    setWidth("400px")
    setDataProvider(chatDataProvider)
  }

  /** Contains list of chatroom menbers */
  val memberList = new java.util.ArrayList[String]()
  val memberDataProvider: ListDataProvider[String] = DataProvider.ofCollection[String](memberList)
  val memberPanel: ListBox[String] = new ListBox[String] {
    // todo    setWidth("100px")
    // todo    setCaption("Chatroom Members")
    setDataProvider(memberDataProvider)
  }

  /** Contains username */
  val userName = new TextField()

  val loginPanel: HorizontalLayout = new HorizontalLayout {
    setSpacing(true)
    add(
      userName,
      new Button("Login", _ => strategy.login(userName.getValue, self))
    )
  }

  val logoutBtn = new Button("Logout", _ => strategy.logout(userName.getValue, self))

  val messagePanel: HorizontalLayout = new HorizontalLayout {
    setSpacing(true)
    val text = new TextField()
    add(
      text,
      new Button(
        "Send", _ => {
          strategy.send(userName.getValue, text.getValue, self)
          text.setValue("")
          text.focus()
        }
      ))
  }

  /** Contains user interface for login/logout and sending of messages */
  val userPanel = // todo new Panel()
    new VerticalLayout {
      setSpacing(true)
      setMargin(true)
      add(
        new HorizontalLayout {
          setSpacing(true)
          add(loginPanel, logoutBtn)
        },
        new HorizontalLayout {
          setSpacing(true)
          add(
            messagePanel,
            new Button(
              "Clear", _ => {
                chatList.clear()
                chatDataProvider.refreshAll()
              }
            ))
        })
    }

  override def initContent(): Component = new VerticalLayout {
    add(
      new Label {
        setText(title)
        // todo        addStyleName(ValoTheme.LABEL_H1)
      },
      new HorizontalLayout {
        setSpacing(true)
        add(
          new VerticalLayout {
            setSpacing(true)
            add(userPanel, chatPanel)
          },
          memberPanel)
      })
  }

  self ! ChatServer.SubscriptionCancelled("")

  override def receive: Receive = {
    // User entered chatroom, update member list
    case ChatServer.Enter(name) =>
      Notification.show(s"$name entered the chatroom", 0, Position.MIDDLE)
      ChatServer.chatServer ! ChatServer.RequestMembers
    // User left chatroom, update member list
    case ChatServer.Leave(name) =>
      Notification.show(s"$name left the chatroom", 0, Position.MIDDLE)
      ChatServer.chatServer ! ChatServer.RequestMembers
    // Message from chatroom, update message list
    case statement: ChatServer.Statement =>
      chatList.add(statement)
      chatDataProvider.refreshAll()
    // todo      chatPanel.scrollToEnd()
    // Member list of chatroom, update member list
    case ChatServer.Members(members) =>
      memberList.clear()
      memberList.addAll(members.asJava)
      memberDataProvider.refreshAll()
    // Subscription successful, adjust user interface state
    case ChatServer.SubscriptionSuccess(name) =>
      ChatServer.chatServer ! ChatServer.RequestMembers
      // todo      loginPanel. setEnabled(false)
      logoutBtn.setEnabled(true)
    // todo      messagePanel.setEnabled(true)
    // todo      userPanel.setCaption(s"Logged in: $name")
    // Subscription cancelled, adjust user interface state
    case ChatServer.SubscriptionCancelled(_) =>
      self ! ChatServer.Members(Nil)
      // todo      loginPanel.setEnabled(true)
      logoutBtn.setEnabled(false)
    // todo      messagePanel.setEnabled(false)
    // todo      userPanel.setCaption("Please login ...")
    // Subscription failed, show warning
    case ChatServer.SubscriptionFailure(error) =>
      Notification.show(error, 0, Position.MIDDLE)
  }

}
