package org.vaadin.addons.vaactor

import Vaactor._
import com.vaadin.flow.component.{ AttachEvent, Component, DetachEvent }

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }

/** Contains some utility traits and the [[VaactorProxyActor]] class.
  *
  * @author Otto Ringhofer
  */
object Vaactor {

  /** Proxy actor for [[Vaactor]],
    * calls [[Vaactor.receiveMessage]] of dedicated [[Vaactor]] with every message received.
    *
    * [[Vaactor.receiveMessage]] calls [[Vaactor.receive]] function
    * synchronized by `access` method of dedicated [[Vaactor.vaactorUI]].
    *
    * @param vaactor dedicated [[Vaactor]]
    */
  class VaactorProxyActor(vaactor: Vaactor) extends Actor {

    def receive: Receive = {
      // catch all messages and forward to UI
      case msg: Any => vaactor.receiveMessage(msg, sender)
    }

  }

  /** Vaadin component with dedicated actor.
    *
    * Extends [[Vaactor]] with termination of actor in `detach` method of component.
    */
  trait VaactorComponent extends Component with Vaactor {

    abstract override def onDetach(detachEvent: DetachEvent): Unit = {
      self ! PoisonPill
      super.onDetach(detachEvent)
    }

  }

  /** VaadinUI with dedicated actor.
    *
    * Bound to selftpe [[VaactorUI]].
    * Extends [[Vaactor]] with automatic definition of vaactorUI as `this`.
    */
  trait UIVaactor extends Vaactor {
    this: VaactorUI =>
    override val vaactorUI: VaactorUI = this
  }

  /** Vaadin Component with dedicated actor and automatic attach/detach message to session actor.
    */
  trait AttachSession extends VaactorComponent {

    /** Message sent to session actor on attach of component */
    val attachMessage: Any

    /** Message sent to session actor on detach of component */
    val detachMessage: Any

    abstract override def onAttach(attachEvent: AttachEvent): Unit = {
      super.onAttach(attachEvent)
      send2SessionActor(attachMessage)
    }

    abstract override def onDetach(detachEvent: DetachEvent): Unit = {
      send2SessionActor(detachMessage)
      super.onDetach(detachEvent)
    }

  }

  /** Vaadin Component with dedicated actor and automatic subscription to session actor.
    */
  trait SubscribeSession extends AttachSession {

    /** Subscribe message sent to session actor on attach of component */
    override val attachMessage: Any = VaactorSession.Subscribe

    /** Unsubscribe message sent to session actor on detach of component */
    override val detachMessage: Any = VaactorSession.Unsubscribe

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

  /** Handles all messages not handled by [[receive]].
    *
    * Overriding is encouraged.
    * Default implementation forwards message to deadLetters actor of actor system.
    *
    * @param msg    message
    * @param sender sender of message
    */
  def orElse(msg: Any, sender: ActorRef): Unit =
    VaactorServlet.system.deadLetters.tell(msg, sender) // forward not possible, no ActorContext available

  /** Call [[receive]] of this trait synchronized by VaactorUI.access.
    * Is used by [[Vaactor.VaactorProxyActor]] to forward received messages to this trait.
    *
    * @param msg    message
    * @param sender sender of message
    */
  def receiveMessage(msg: Any, sender: ActorRef): Unit = {
    _sender = sender
    vaactorUI.vaadinUI.access(() => receive.applyOrElse(msg, (mp: Any) => orElse(mp, sender)))
    _sender = Actor.noSender
  }

  /** Receive function, is called in context of VaadinUI (via ui.access) */
  def receive: Actor.Receive

  /** Send a message to the session actor.
    *
    * No message is sent, if [[VaactorServlet.sessionProps]] is None
    *
    * @param msg message to be sent
    */
  def send2SessionActor(msg: Any): Unit = vaactorUI.send2SessionActor(msg, self)

}
