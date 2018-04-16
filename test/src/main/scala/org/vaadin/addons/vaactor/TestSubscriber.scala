package org.vaadin.addons.vaactor

import TestServlet._
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.orderedlayout.VerticalLayout

import akka.actor.Actor.Receive

class TestSubscriber(override val vaactorUI: VaactorUI) extends VerticalLayout with Vaactor.AttachSession {

  override val attachMessage: Any = Attach
  override val detachMessage: Any = Detach

  add(new Label("I'm here"))

  override def receive: Receive = {
    case _ =>
  }

}
