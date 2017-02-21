package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor.VaactorUI
import com.vaadin.ui._
import com.vaadin.server.{ Sizeable, VaadinRequest }

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
class ChatUI extends VaactorUI {

  /** contains list of messages from chatroom */
  val chatPanel = new Grid {
    setCaption("Chat")
    // TODO addColumn[String]("User")
    // TODO addColumn[String]("Message")
    setWidth(400, Sizeable.Unit.PIXELS)
  }

  /** contains user interface for login/logout and sending of messages */
  val userPanel = new ChatComponent(this)

  /** contains list of chatroom menbers */
  val memberPanel = new ListSelect {
    setCaption("Chatroom Members")
    setWidth(100, Sizeable.Unit.PIXELS)
    // TODO nullSelectionAllowed = false
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

  def receive = {
    // session state, display and send to user panel actor
    case state: ChatSession.State =>
      userPanel.setCaption(if (state.isLoggedIn) s"Session - Welcome ${ state.name }" else "Session")
      userPanel.self ! state
    // user entered chatroom, update member list
    case e @ ChatServer.Enter(name) =>
      // TODO memberPanel.addItem(name)
      Notification.show(s"$name entered the chatroom")
    // user left chatroom, update member list
    case l @ ChatServer.Leave(name) =>
      // TODO memberPanel.removeItem(name)
      Notification.show(s"$name left the chatroom")
    //  message from chatroom, update message list
    case s @ ChatServer.Statement(name, msg) =>
      // TODO chatPanel.addRow(name, msg)
      // TODO chatPanel.recalculateColumnWidths()
      chatPanel.scrollToEnd()
    // member list of chatroom, update member list
    case m @ ChatServer.Members(members) =>
    // TODO memberPanel.removeAllItems()
    // TODO for (m <- members) memberPanel.addItem(m)
    // clear, clear message list
    case ChatUI.Clear =>
    // TODO chatPanel.container.removeAllItems()
  }

}
