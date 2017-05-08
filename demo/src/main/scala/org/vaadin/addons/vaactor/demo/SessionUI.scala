package org.vaadin.addons.vaactor.demo

import org.vaadin.addons.vaactor._
import org.vaadin.addons.vaactor.chat.ChatComponent
import com.vaadin.annotations.Push
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.ui.Transport
import com.vaadin.ui._

import akka.actor.ActorRef

object SessionUI {

  class Strategy(session: ActorRef) extends ChatComponent.Strategy {

    override def login(name: String, sender: ActorRef): Unit =
      session.tell(Session.Login(name), sender)

    override def logout(name: String, sender: ActorRef): Unit =
      session.tell(Session.Logout, sender)

    override def send(name: String, text: String, sender: ActorRef): Unit =
      session.tell(Session.Message(text), sender)

  }

}

/** UI for Vaactor chat with session support
  *
  * @author Otto Ringhofer
  */
@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
class SessionUI extends VaactorUI {

  override def init(request: VaadinRequest): Unit = {
    val chatComponent = sessionActor map { actor =>
      val strategy = new demo.SessionUI.Strategy(actor)
      new ChatComponent(this, "Vaactor chat with session support", strategy)
    }
    setContent(chatComponent.getOrElse(new Label("Servlet din't define sessionProps ;-(")))
  }

}
