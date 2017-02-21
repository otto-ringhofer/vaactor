package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor.VaactorUI
import org.vaadin.addons.vaactor.demo.ChatServer.Statement
import com.vaadin.annotations.Push
import com.vaadin.data.provider.{ DataProvider, ListDataProvider }
import com.vaadin.server.{ Sizeable, VaadinRequest }
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.ui.Transport
import com.vaadin.ui._

import scala.collection.JavaConverters._

/** contains ui messages
  *
  * @author Otto Ringhofer
  */
object ChatUI {

  /** clear message list */
  case object Clear

}

/** ui to be created by servlet
  *
  * @author Otto Ringhofer
  */
@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET)
class ChatUI extends VaactorUI {

  /** contains list of messages from chatroom */
  val chatList = new java.util.ArrayList[Statement]()
  val chatDataProvider: ListDataProvider[Statement] = DataProvider.ofCollection[Statement](chatList)
  val chatPanel = new Grid[Statement]("Chat", chatDataProvider) {
    addColumn(d => d.name)
    addColumn(d => d.msg)
    setWidth(400, Sizeable.Unit.PIXELS)
  }

  /** contains user interface for login/logout and sending of messages */
  val userPanel = new ChatComponent(this)

  /** contains list of chatroom menbers */
  val memberList = new java.util.ArrayList[String]()
  val memberDataProvider: ListDataProvider[String] = DataProvider.ofCollection[String](memberList)
  val memberPanel = new ListSelect("Chatroom Members", memberDataProvider) {
    setWidth(100, Sizeable.Unit.PIXELS)
  }

  override def initVaactorUI(request: VaadinRequest): Unit = {
    setContent(new VerticalLayout {
      setSpacing(true)
      setMargin(true)
      addComponent(new HorizontalLayout {
        setSpacing(true)
        addComponent(new VerticalLayout {
          setSpacing(true)
          addComponent(userPanel)
          addComponent(chatPanel)
        })
        addComponent(memberPanel)
      })
    })
    sessionActor ! ChatSession.Login()
  }

  def receive: PartialFunction[Any, Unit] = {
    // session state, display and send to user panel actor
    case state: ChatSession.State =>
      userPanel.setCaption(if (state.isLoggedIn) s"Session - Welcome ${ state.name }" else "Session")
      userPanel.self ! state
    // user entered chatroom, update member list
    case ChatServer.Enter(name) =>
      memberList.add(name)
      memberPanel.setDataProvider(memberDataProvider)
      Notification.show(s"$name entered the chatroom")
    // user left chatroom, update member list
    case ChatServer.Leave(name) =>
      memberList.remove(name)
      memberPanel.setDataProvider(memberDataProvider)
      Notification.show(s"$name left the chatroom")
    //  message from chatroom, update message list
    case statement: ChatServer.Statement =>
      chatList.add(statement)
      chatPanel.setDataProvider(chatDataProvider)
      chatPanel.scrollToEnd()
    // member list of chatroom, update member list
    case ChatServer.Members(members) =>
      memberList.clear()
      memberList.addAll(members.asJava)
      memberPanel.setDataProvider(memberDataProvider)
    // clear, clear message list
    case ChatUI.Clear =>
      chatList.clear()
      chatPanel.setDataProvider(chatDataProvider)
  }

}
