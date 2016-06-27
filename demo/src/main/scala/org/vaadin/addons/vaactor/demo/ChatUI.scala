package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor.VaactorUI

import vaadin.scala._
import vaadin.scala.server.ScaladinRequest

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
    caption = "Chat"
    addColumn[String]("User")
    addColumn[String]("Message")
    width = 400.px
  }

  /** contains user interface for login/logout and sending of messages */
  val userPanel = new ChatComponent(this)

  /** contains list of chatroom menbers */
  val memberPanel = new ListSelect {
    caption = "Chatroom Members"
    width = 100.px
    nullSelectionAllowed = false
  }

  override def initVaactorUI(request: ScaladinRequest): Unit = {
    content = new VerticalLayout {
      spacing = true
      margin = true
      add(new HorizontalLayout {
        spacing = true
        add(new VerticalLayout {
          spacing = true
          add(userPanel)
          add(chatPanel)
        })
        add(memberPanel)
      })
    }
    sessionActor ! ChatSession.Login()
  }

  def receive = {
    // session state, display and send to user panel actor
    case state: ChatSession.State =>
      userPanel.caption = if (state.isLoggedIn) s"Session - Welcome ${ state.name }" else "Session"
      userPanel.self ! state
    // user entered chatroom, update member list
    case e @ ChatServer.Enter(name) =>
      memberPanel.addItem(name)
      Notification.show(s"$name entered the chatroom")
    // user left chatroom, update member list
    case l @ ChatServer.Leave(name) =>
      memberPanel.removeItem(name)
      Notification.show(s"$name left the chatroom")
    //  message from chatroom, update message list
    case s @ ChatServer.Statement(name, msg) =>
      chatPanel.addRow(name, msg)
      chatPanel.recalculateColumnWidths()
      chatPanel.scrollToEnd()
    // member list of chatroom, update member list
    case m @ ChatServer.Members(members) =>
      memberPanel.removeAllItems()
      for (m <- members) memberPanel.addItem(m)
    // clear, clear message list
    case ChatUI.Clear =>
      chatPanel.container.removeAllItems()
  }

}
