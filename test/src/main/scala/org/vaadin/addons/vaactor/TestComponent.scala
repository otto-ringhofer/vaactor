package org.vaadin.addons.vaactor

import Forwarder._
import TestComponent._
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextField

import akka.actor.Actor.Receive
import akka.actor._

object TestComponent {

  val CompTextName = "comp-text"
  val CompButtonName = "comp-button"

  case class RequestText(replyTo: ActorRef)

  case object RequestText

  case class ReplyText(txt: String)

}

class TestComponent(val vaactorUI: VaactorUI, nameSuffix: String)
  extends VerticalLayout with Vaactor.VaactorComponent {

  val txt = new TextField()
  txt.setWidth("100%")
  txt.setId(CompTextName + nameSuffix)

  val btn = new Button("Send to Session" + nameSuffix)
  btn.setId(CompButtonName + nameSuffix)
  btn.addClickListener { _ => send2SessionActor(TestServlet.SessionState(txt.getValue)) }

  // todo  setCaption("TestComponent" + nameSuffix)
  add(txt, btn)

  override def onAttach(attachEvent: AttachEvent): Unit = {
    super.onAttach(attachEvent)
    forwarder.tell(Register(VaactorActorName + nameSuffix), self)
  }

  override def receive: Receive = {
    case RequestText(replyTo) => replyTo ! ReplyText(txt.getValue)
    case RequestText => sender ! ReplyText(txt.getValue)
  }

}
