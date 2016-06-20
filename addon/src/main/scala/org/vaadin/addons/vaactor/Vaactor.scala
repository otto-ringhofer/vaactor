package org.vaadin.addons.vaactor

import akka.actor.{ Actor, Props }

trait Vaactor {
  vaactor =>

  /** VaactorUI of this component, used for access of sessionActor and access method */
  val vaactorUI: VaactorUI

  /** actor for this Vaactor */
  // implicit injects the `self` ActorRef as sender to `!` function of `ActorRef`
  implicit val self = VaactorUI.actorOf(Props(classOf[VaactorActor], vaactor))

  private def logUnprocessed: Actor.Receive = {
    case msg: Any =>
  }

  // lazy because receive is not yet initialized
  private lazy val receiveWorker = receive orElse logUnprocessed

  // forward message to receive function of ui, undefined messages are forwarded to logUnprocessed
  private[vaactor] def receiveMessage(msg: Any): Unit = vaactorUI.access(receiveWorker(msg))

  def receive: Actor.Receive

}

private class VaactorActor(vaactor: Vaactor) extends Actor {

  def receive = {
    // catch all messages and forward to UI
    case msg: Any =>
      vaactor.receiveMessage(msg)
  }

}
