package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor._
import org.vaadin.addons.vaactor.chat.ChatComponent
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.Route
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo

import akka.actor.ActorRef

object SessionUI {

  class Strategy(hasSession: Vaactor.HasSession) extends ChatComponent.Strategy {

    override def login(name: String, sender: ActorRef): Unit =
      hasSession.session.tell(Session.Login(name), sender)

    override def logout(name: String, sender: ActorRef): Unit =
      hasSession.session.tell(Session.Logout, sender)

    override def send(name: String, text: String, sender: ActorRef): Unit =
      hasSession.session.tell(Session.Message(text), sender)

  }

}

/** UI for Vaactor chat with session support
  *
  * @author Otto Ringhofer
  */
@Route("session")
@Theme(value = classOf[Lumo], variant = Lumo.DARK)
@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET)
class SessionUI extends VerticalLayout with Vaactor.HasSession {

  val strategy = new demo.SessionUI.Strategy(this) // "this" ssupports strategy with session actor
  val chatComponent: ChatComponent = new ChatComponent("Vaactor chat with session support", strategy)
    with Vaactor.AttachSession { // AttachSession

    /** Send to session actor on attach */
    override val attachMessage: Any = Session.Attached

    /** Send to session actor on detach */
    override val detachMessage: Any = Session.Detached

  }

  add(chatComponent)

}
