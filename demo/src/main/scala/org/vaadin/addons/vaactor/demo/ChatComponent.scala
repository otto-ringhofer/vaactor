package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor.{ Vaactor, VaactorUI }

import vaadin.scala._

class ChatComponent(val vaactorUI: VaactorUI) extends Panel with Vaactor {

  val loginPanel = new HorizontalLayout {
    spacing = true
    val text = add(new TextField())
    add(new Button {
      caption = "Login"
      clickListeners += {
        val msg = ChatSession.Login(text.value.getOrElse(""))
        vaactorUI.sessionActor ! msg
      }
    })
  }

  val logoutBtn = new Button {
    caption = "Logout"
    clickListeners += {
      val msg = ChatSession.Logout
      vaactorUI.sessionActor ! msg
    }
  }

  val messagePanel = new HorizontalLayout {
    spacing = true
    val text = add(new TextField())
    add(new Button {
      caption = "Send"
      clickListeners += {
        val msg = ChatSession.Message(text.value.getOrElse(""))
        text.value = ""
        text.focus()
        vaactorUI.sessionActor ! msg
      }
    })
  }

  content = new VerticalLayout {
    spacing = true
    margin = true
    add(new HorizontalLayout {
      spacing = true
      add(loginPanel)
      add(logoutBtn)
    })
    add(new HorizontalLayout {
      spacing = true
      add(messagePanel)
      add(new Button {
        caption = "Clear"
        clickListeners += { e => vaactorUI.self ! ChatUI.Clear }
      })
    })
  }

  def receive = {
    case state: ChatSession.State =>
      loginPanel.enabled = !state.isLoggedIn
      logoutBtn.enabled = state.isLoggedIn
      messagePanel.enabled = state.isLoggedIn
  }

}
