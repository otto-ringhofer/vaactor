package org.vaadin.addons.vaactor.direct

import org.vaadin.addons.vaactor.chat.{ ChatComponent, ChatServer }
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.{ BodySize, Push }
import com.vaadin.flow.router.Route
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo

import akka.actor.ActorRef

object DirectUI {

  val strategy: ChatComponent.Strategy = new ChatComponent.Strategy {

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
@BodySize(height = "100vh", width = "100vw")
@Route("direct")
@Theme(classOf[Lumo])
@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
class DirectUI extends VerticalLayout {

  add(new ChatComponent("Vaactor chat direct (without session)", DirectUI.strategy))

}
