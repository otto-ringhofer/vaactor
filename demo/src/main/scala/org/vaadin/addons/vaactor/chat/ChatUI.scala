package org.vaadin.addons.vaactor.chat

import org.vaadin.addons.vaactor._
import org.vaadin.addons.vaactor.chat.ChatServer._
import com.vaadin.annotations.Push
import com.vaadin.data.provider.{ DataProvider, ListDataProvider }
import com.vaadin.server.{ Sizeable, VaadinRequest }
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.ui.Transport
import com.vaadin.ui._

import scala.collection.JavaConverters._

/** UI to be created by servlet
  *
  * @author Otto Ringhofer
  */
@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
class ChatUI extends VaactorUI with Vaactor.UIVaactor {

  /** Contains list of messages from chatroom */
  val chatList = new java.util.ArrayList[Statement]()
  val chatDataProvider: ListDataProvider[Statement] = DataProvider.ofCollection[Statement](chatList)
  val chatPanel = new Grid[Statement]("Chat", chatDataProvider) {
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
      new Button("Login", _ => chatServer ! Subscribe(Client(userName.getValue, self)))
    )
  }

  val logoutBtn = new Button("Logout", _ => logout(userName.getValue))

  val messagePanel = new HorizontalLayout {
    setSpacing(true)
    val text = new TextField()
    addComponents(
      text,
      new Button(
        "Send", _ => {
          chatServer ! Statement(userName.getValue, text.getValue)
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

  override def init(request: VaadinRequest): Unit = {
    setContent(new HorizontalLayout {
      setSpacing(true)
      addComponents(
        new VerticalLayout {
          setSpacing(true)
          addComponents(userPanel, chatPanel)
        },
        memberPanel)
    })
    logout("")
  }

  private def logout(name: String): Unit = {
    if (name.nonEmpty) chatServer ! Unsubscribe(Client(name, self))
    userName.setValue("")
    self ! Members(Nil)
    loginPanel.setEnabled(true)
    logoutBtn.setEnabled(false)
    messagePanel.setEnabled(false)
    userPanel.setCaption("Please login ...")
  }

  def receive: PartialFunction[Any, Unit] = {
    // User entered chatroom, update member list
    case Enter(name) =>
      memberList.add(name)
      memberDataProvider.refreshAll() // refreshItem operates only on changed items, not on new items
      Notification.show(s"$name entered the chatroom")
    // User left chatroom, update member list
    case Leave(name) =>
      memberList.remove(name)
      memberDataProvider.refreshAll()
      Notification.show(s"$name left the chatroom")
    // Message from chatroom, update message list
    case statement: Statement =>
      chatList.add(statement)
      chatDataProvider.refreshAll()
      chatPanel.scrollToEnd()
    // Member list of chatroom, update member list
    case Members(members) =>
      memberList.clear()
      memberList.addAll(members.asJava)
      memberDataProvider.refreshAll()
    // Subscription successful, adjust user interface state
    case SubscriptionSuccess(welcome) =>
      chatServer ! RequestMembers
      loginPanel.setEnabled(false)
      logoutBtn.setEnabled(true)
      messagePanel.setEnabled(true)
      userPanel.setCaption(welcome)
    // Subscription failed, show warning
    case SubscriptionFailure(error) =>
      Notification.show(error, Notification.Type.WARNING_MESSAGE)
  }

}
