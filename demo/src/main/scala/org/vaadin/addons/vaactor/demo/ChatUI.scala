package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor.VaactorUI

import vaadin.scala._
import vaadin.scala.server.ScaladinRequest

object ChatUI {

  case object Clear

}

class ChatUI extends VaactorUI {

  val chatPanel = new Grid {
    caption = "Chat"
    addColumn[String]("User")
    addColumn[String]("Message")
    width = 400.px
  }

  val userPanel = new ChatComponent(this)

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
    case state: ChatSession.State =>
      userPanel.caption = if (state.isLoggedIn) s"Session - Welcome ${ state.name }" else "Session"
      userPanel.self ! state
    case e @ ChatServer.Enter(name) =>
      memberPanel.addItem(name)
      Notification.show(s"$name entered the chatroom")
    case l @ ChatServer.Leave(name) =>
      memberPanel.removeItem(name)
      Notification.show(s"$name left the chatroom")
    case s @ ChatServer.Statement(name, msg) =>
      chatPanel.addRow(name, msg)
      chatPanel.recalculateColumnWidths()
      chatPanel.scrollToEnd()
    case m @ ChatServer.Members(members) =>
      memberPanel.removeAllItems()
      for (m <- members) memberPanel.addItem(m)
    case ChatUI.Clear =>
      chatPanel.container.removeAllItems()
  }

}
