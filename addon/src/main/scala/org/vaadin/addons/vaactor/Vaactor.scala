package org.vaadin.addons.vaactor

import Vaactor._
import com.vaadin.ui.Component

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }

/** Contains [[VaactorProxyActor]] class */
object Vaactor {

  /** Proxy actor for [[Vaactor]],
    * calls [[Vaactor.receiveMessage]] of dedicated [[Vaactor]] wirh all messages received.
    *
    * [[Vaactor.receiveMessage]] calls [[Vaactor.receive]] function
    * synchronized by `access` method of dedicated [[Vaactor]].
    *
    * @param vaactor dedicated [[Vaactor]]
    */
  class VaactorProxyActor(vaactor: Vaactor) extends Actor {

    def receive: PartialFunction[Any, Unit] = {
      // catch all messages and forward to UI
      case msg: Any => vaactor.receiveMessage(msg, sender)
    }

  }

}

/** Makes a class "feel" like an actor, with `receive` method synchronized with VaadinUI
  *
  * Creates actor, assigns it to implicit `self` value,
  * `receive` is called in context of VaadinUI
  *
  * @author Otto Ringhofer
  */
trait Vaactor {

  private var _sender: ActorRef = Actor.noSender

  /** VaactorUI of this component, used for access of sessionActor and access method */
  val vaactorUI: VaactorUI

  /** Actor dedicated to this Vaactor */
  // implicit injects the `self` ActorRef as sender to `!` function of `ActorRef`
  implicit lazy val self: ActorRef = vaactorUI.actorOf(Props(classOf[VaactorProxyActor], this))

  /** The reference sender Actor of the last received message.
    *
    * WARNING: Only valid within [[receive]] of the Vaactor itself,
    * so do not close over it and publish it to other threads!
    */
  def sender: ActorRef = _sender

  // forward message to receive function of ui, undefined messages are forwarded to logUnprocessed
  private[vaactor] def receiveMessage(msg: Any, sender: ActorRef): Unit = {
    _sender = sender
    vaactorUI.access(() => receive(msg))
    _sender = Actor.noSender
  }

  /** Receive function, is called in context of VaadinUI (via ui.access) */
  def receive: Actor.Receive

}

/** Vaadin component with dedicated actor.
  *
  * Extends [[Vaactor]] with terminating of actor in `detach` methode of component.
  */
trait VaactorComponent extends Vaactor with Component {

  abstract override def detach(): Unit = {
    self ! PoisonPill
    super.detach()
  }

}
