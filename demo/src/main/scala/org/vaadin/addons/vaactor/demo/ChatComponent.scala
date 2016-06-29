package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor.{ Vaactor, VaactorUI }

import vaadin.scala._

/** component handles login/logout and sending of messages
  *
  * @param vaactorUI ui to be used by [[Vaactor]], contains reference to session actor
  * @author Otto Ringhofer
  */
class ChatComponent(val vaactorUI: VaactorUI) extends Panel with Vaactor {

  val loginPanel = new HorizontalLayout {
    spacing = true
    val text = add(new TextField())
    add(new Button {
      caption = "Login"
      // send login message to session actor
      clickListeners += {
        val msg = ChatSession.Login(text.value.getOrElse(""))
        vaactorUI.sessionActor ! msg
      }
    })
  }

  val logoutBtn = new Button {
    caption = "Logout"
    // send logout message to session actor
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
      // send chat message to session actor, will be augmented with username and sent to chatroom
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
        // send clear message to ui
        clickListeners += { vaactorUI.self ! ChatUI.Clear }
      })
    })
  }

  def receive = {
    // session state, adjust user interface depending on logged-in state
    case state: ChatSession.State =>
      loginPanel.enabled = !state.isLoggedIn
      logoutBtn.enabled = state.isLoggedIn
      messagePanel.enabled = state.isLoggedIn
  }

}
