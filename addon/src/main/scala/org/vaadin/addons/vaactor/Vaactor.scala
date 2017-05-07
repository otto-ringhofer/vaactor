package org.vaadin.addons.vaactor

import akka.actor.{ Actor, ActorRef, Props }

/** makes a class "feel" like an actor, but synchronized with vaadin ui
  *
  * creates actor, assigns it to implicit `self` value
  * `receive` is called in context of vaadin ui
  *
  * @author Otto Ringhofer
  */
trait Vaactor {
  vaactor =>

  /** VaactorUI of this component, used for access of sessionActor and access method */
  val vaactorUI: VaactorUI

  /** actor for this Vaactor */
  // implicit injects the `self` ActorRef as sender to `!` function of `ActorRef`
  implicit lazy val self: ActorRef = vaactorUI.actorOf(Props(classOf[VaactorActor], vaactor))

  private def logUnprocessed: Actor.Receive = {
    case _ =>
  }

  // lazy because receive is not yet initialized
  private lazy val receiveWorker = receive orElse logUnprocessed

  // forward message to receive function of ui, undefined messages are forwarded to logUnprocessed
  private[vaactor] def receiveMessage(msg: Any): Unit = vaactorUI.access(() => receiveWorker(msg))

  /** receive function, is called in context of vaadin ui (via ui.access) */
  def receive: Actor.Receive

}

private class VaactorActor(vaactor: Vaactor) extends Actor {

  def receive: PartialFunction[Any, Unit] = {
    // catch all messages and forward to UI
    case msg: Any =>
      vaactor.receiveMessage(msg)
  }

}
