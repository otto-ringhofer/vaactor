package org.vaadin.addons.vaactor.direct

import org.vaadin.addons.vaactor.VaactorUI
import org.vaadin.addons.vaactor.chat.{ ChatComponent, ChatServer }
import com.vaadin.annotations.Push
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.ui.Transport

import akka.actor.ActorRef

object DirectUI {

  val strategy = new ChatComponent.Strategy {

    override def login(name: String, sender: ActorRef): Unit =
      ChatServer.chatServer.tell(ChatServer.Subscribe(ChatServer.Client(name, sender)), sender)

    override def logout(name: String, sender: ActorRef): Unit =
      ChatServer.chatServer.tell(ChatServer.Unsubscribe(ChatServer.Client(name, sender)), sender)

    override def send(name: String, text: String, sender: ActorRef): Unit =
      ChatServer.chatServer.tell(ChatServer.Statement(name, text), sender)

  }

}

/** UI for Vaactor chat direct (without session)
  *
  * @author Otto Ringhofer
  */
@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
class DirectUI extends VaactorUI {

  override def init(request: VaadinRequest): Unit =
    setContent(new ChatComponent(this, "Vaactor chat direct (without session)", DirectUI.strategy))

}
