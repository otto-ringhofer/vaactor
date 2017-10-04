package org.vaadin.addons.vaactor.example

import org.vaadin.addons.vaactor._
import com.vaadin.ui._

import akka.actor.Actor.Receive

class StateButton(val vaactorUI: VaactorUI) extends Button with Vaactor {

  setCaption("SessionState")
  addClickListener { _ => vaactorUI.sessionActor ! VaactorSession.RequestSessionState }

  override def receive: Receive = {
    case state: Int => setCaption(s"SessionState is $state")
  }

}
