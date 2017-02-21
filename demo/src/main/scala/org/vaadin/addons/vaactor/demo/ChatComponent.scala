package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor.{ Vaactor, VaactorUI }
import com.vaadin.ui._

/** component handles login/logout and sending of messages
  *
  * @param vaactorUI ui to be used by [[Vaactor]], contains reference to session actor
  * @author Otto Ringhofer
  */
class ChatComponent(val vaactorUI: VaactorUI) extends Panel with Vaactor {

  val loginPanel = new HorizontalLayout {
    setSpacing(true)
    val text = new TextField()
    addComponent(text)
    addComponent(new Button(
      "Login",
      // send login message to session actor
      _ => {
        val msg = ChatSession.Login(text.getValue)
        vaactorUI.sessionActor ! msg
      })
    )
  }

  val logoutBtn = new Button(
    "Logout",
    // send logout message to session actor
    _ => {
      val msg = ChatSession.Logout
      vaactorUI.sessionActor ! msg
    }
  )

  val messagePanel = new HorizontalLayout {
    setSpacing(true)
    val text = new TextField()
    addComponent(text)
    addComponent(new Button(
      "Send",
      // send chat message to session actor, will be augmented with username and sent to chatroom
      _ => {
        val msg = ChatSession.Message(text.getValue)
        text.setValue("")
        text.focus()
        vaactorUI.sessionActor ! msg
      }
    ))
  }

  setContent(new VerticalLayout {
    setSpacing(true)
    setMargin(true)
    addComponent(new HorizontalLayout {
      setSpacing(true)
      addComponent(loginPanel)
      addComponent(logoutBtn)
    })
    addComponent(new HorizontalLayout {
      setSpacing(true)
      addComponent(messagePanel)
      addComponent(new Button(
        "Clear",
        // send clear message to ui
        _ => { vaactorUI.self ! ChatUI.Clear }
      ))
    })
  })

  def receive = {
    // session state, adjust user interface depending on logged-in state
    case state: ChatSession.State =>
      loginPanel.setEnabled(!state.isLoggedIn)
      logoutBtn.setEnabled(state.isLoggedIn)
      messagePanel.setEnabled(state.isLoggedIn)
  }

}
