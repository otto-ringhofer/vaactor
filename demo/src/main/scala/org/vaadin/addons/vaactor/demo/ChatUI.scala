package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor.VaactorsUI

import vaadin.scala._
import vaadin.scala.server.ScaladinRequest

class ChatUI extends VaactorsUI {

  val loginPanel = new HorizontalLayout {
    spacing = true
    val text = add(new TextField())
    add(new Button {
      caption = "Login"
      clickListeners += {
        val msg = ChatSession.Login(text.value.getOrElse(""))
        sessionActor ! msg
      }
    })
  }

  val chatPanel = new Grid {
    caption = "Chat"
    addColumn[String]("User")
    addColumn[String]("Message")
    width = 400.px
  }

  val messagePanel = new HorizontalLayout {
    spacing = true
    val text = add(new TextField())
    add(new Button {
      caption = "Send"
      clickListeners += {
        val msg = ChatSession.Message(text.value.getOrElse(""))
        sessionActor ! msg
      }
    })
    add(new Button {
      caption = "Clear"
      clickListeners += { e => chatPanel.container.removeAllItems() }
    })
  }

  val logoutBtn = new Button {
    caption = "Logout"
    clickListeners += {
      val msg = ChatSession.Logout
      sessionActor ! msg
    }
  }

  val userPanel = new Panel {
    content = new VerticalLayout {
      spacing = true
      margin = true
      add(new HorizontalLayout {
        spacing = true
        add(loginPanel)
        add(logoutBtn)
      })
      add(messagePanel)
    }
  }

  val memberPanel = new ListSelect {
    caption = "Chatroom Members"
    width = 100.px
    nullSelectionAllowed = false
  }

  override def initVaactorsUI(request: ScaladinRequest): Unit = {
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
      loginPanel.enabled = !state.isLoggedIn
      logoutBtn.enabled = state.isLoggedIn
      messagePanel.enabled = state.isLoggedIn
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
  }

}
