package org.vaadin.addons.vaactor

import Forwarder._
import TestComponent._
import com.vaadin.ui.{ Button, TextField, VerticalLayout }

import akka.actor.Actor.Receive
import akka.actor._

object TestComponent {

  val CompTextName = "comp-text"
  val CompButtonName = "comp-button"

  case class RequestText(replyTo: ActorRef)

  case class ReplyText(txt: String)

}

class TestComponent(val vaactorUI: VaactorUI, nameSuffix: String)
  extends VerticalLayout with VaactorComponent {

  val txt = new TextField()
  txt.setWidth("100%")
  txt.setId(CompTextName + nameSuffix)

  val btn = new Button("Send to Session" + nameSuffix)
  btn.setId(CompButtonName + nameSuffix)
  btn.addClickListener { _ => send2SessionActor(TestServlet.SessionState(txt.getValue)) }

  setCaption("TestComponent" + nameSuffix)
  addComponents(txt, btn)

  override def attach(): Unit = {
    super.attach()
    forwarder.tell(Register(VaactorActorName + nameSuffix), self)
  }

  override def receive: Receive = {
    case RequestText(replyTo) => replyTo ! ReplyText(txt.getValue)
  }

}
