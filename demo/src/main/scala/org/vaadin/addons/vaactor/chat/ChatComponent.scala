package org.vaadin.addons.vaactor.chat

import org.vaadin.addons.vaactor._
import org.vaadin.addons.vaactor.demo.Session
import com.vaadin.annotations.Push
import com.vaadin.data.provider.{ DataProvider, ListDataProvider }
import com.vaadin.server.Sizeable
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.ui.Transport
import com.vaadin.ui._
import com.vaadin.ui.themes.ValoTheme

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
@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
class ChatComponent(override val vaactorUI: VaactorUI, title: String, strategy: ChatComponent.Strategy)
  extends CustomComponent with Vaactor.AttachSession {

  /** Send to session actor on attach */
  override val attachMessage = Session.Attached

  /** Send to session actor on detach */
  override val detachMessage = Session.Detached

  /** Contains list of messages from chatroom */
  val chatList = new java.util.ArrayList[ChatServer.Statement]()
  val chatDataProvider: ListDataProvider[ChatServer.Statement] = DataProvider.ofCollection[ChatServer.Statement](chatList)
  val chatPanel = new Grid[ChatServer.Statement]("Chat", chatDataProvider) {
    addColumn(d => d.name)
    addColumn(d => d.msg)
    setWidth(400, Sizeable.Unit.PIXELS)
  }

  /** Contains list of chatroom menbers */
  val memberList = new java.util.ArrayList[String]()
  val memberDataProvider: ListDataProvider[String] = DataProvider.ofCollection[String](memberList)
  val memberPanel = new ListSelect("Chatroom Members", memberDataProvider) {
    setWidth(100, Sizeable.Unit.PIXELS)
  }

  /** Contains username */
  val userName = new TextField()

  val loginPanel = new HorizontalLayout {
    setSpacing(true)
    addComponents(
      userName,
      new Button("Login", _ => strategy.login(userName.getValue, self))
    )
  }

  val logoutBtn = new Button("Logout", _ => strategy.logout(userName.getValue, self))

  val messagePanel = new HorizontalLayout {
    setSpacing(true)
    val text = new TextField()
    addComponents(
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
  val userPanel = new Panel(
    new VerticalLayout {
      setSpacing(true)
      setMargin(true)
      addComponents(
        new HorizontalLayout {
          setSpacing(true)
          addComponents(loginPanel, logoutBtn)
        },
        new HorizontalLayout {
          setSpacing(true)
          addComponents(
            messagePanel,
            new Button(
              "Clear", _ => {
                chatList.clear()
                chatDataProvider.refreshAll()
              }
            ))
        })
    }
  )

  setCompositionRoot(new VerticalLayout {
    addComponents(
      new Label {
        setValue(title)
        addStyleName(ValoTheme.LABEL_H1)
      },
      new HorizontalLayout {
        setSpacing(true)
        addComponents(
          new VerticalLayout {
            setSpacing(true)
            addComponents(userPanel, chatPanel)
          },
          memberPanel)
      })
  })

  self ! ChatServer.SubscriptionCancelled("")

  override def receive: PartialFunction[Any, Unit] = {
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
      chatList.add(statement)
      chatDataProvider.refreshAll()
      chatPanel.scrollToEnd()
    // Member list of chatroom, update member list
    case ChatServer.Members(members) =>
      memberList.clear()
      memberList.addAll(members.asJava)
      memberDataProvider.refreshAll()
    // Subscription successful, adjust user interface state
    case ChatServer.SubscriptionSuccess(name) =>
      ChatServer.chatServer ! ChatServer.RequestMembers
      loginPanel.setEnabled(false)
      logoutBtn.setEnabled(true)
      messagePanel.setEnabled(true)
      userPanel.setCaption(s"Logged in: $name")
    // Subscription cancelled, adjust user interface state
    case ChatServer.SubscriptionCancelled(_) =>
      self ! ChatServer.Members(Nil)
      loginPanel.setEnabled(true)
      logoutBtn.setEnabled(false)
      messagePanel.setEnabled(false)
      userPanel.setCaption("Please login ...")
    // Subscription failed, show warning
    case ChatServer.SubscriptionFailure(error) =>
      Notification.show(error, Notification.Type.WARNING_MESSAGE)
  }

}
