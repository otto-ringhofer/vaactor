package org.vaadin.addons.vaactor.direct

import org.vaadin.addons.vaactor.VaactorUI
import org.vaadin.addons.vaactor.chat.ChatComponent
import org.vaadin.addons.vaactor.chat.ChatServer._
import com.vaadin.annotations.Push
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.ui.Transport

import akka.actor.ActorRef

object ChatUI {

  val strategy = new ChatComponent.Strategy {

    override def login(name: String, sender: ActorRef): Unit =
      chatServer.tell(Subscribe(Client(name, sender)), sender)

    override def logout(name: String, sender: ActorRef): Unit =
      if (name.nonEmpty) chatServer.tell(Unsubscribe(Client(name, sender)), sender)

    override def send(name: String, text: String, sender: ActorRef): Unit =
      chatServer.tell(Statement(name, text), sender)

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
class ChatUI extends VaactorUI {
  chatUI =>

  override def init(request: VaadinRequest): Unit =
    setContent(new ChatComponent(chatUI, "Vaactor chat direct (without session)", ChatUI.strategy))

}
