package org.vaadin.addons.vaactor

import TestServlet._
import com.vaadin.ui._

import akka.actor.Actor.Receive

class TestSubscriber(override val vaactorUI: VaactorUI) extends VerticalLayout with Vaactor.AttachSession {

  override val attachMessage: Any = Attach
  override val detachMessage: Any = Detach

  addComponent(new Label("I'm here"))

  override def receive: Receive = {
    case _ =>
  }

}
