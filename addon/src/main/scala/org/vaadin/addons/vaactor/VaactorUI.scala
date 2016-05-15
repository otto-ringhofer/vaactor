package org.vaadin.addons.vaactor

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }
import vaadin.scala.server.{ ScaladinRequest, ScaladinSession }
import vaadin.scala.{ PushMode, UI }

/** UI with actors */
abstract class VaactorsUI(title: String = null, theme: String = null, widgetset: String = null,
  preserveOnRefresh: Boolean = false, pushMode: PushMode.Value = PushMode.Automatic)
  extends UI(title, theme, widgetset, preserveOnRefresh, pushMode) {

  private var _sessionActor: ActorRef = _
  private var _uiActor: ActorRef = _

  /** session actor for this UI */
  // lazy because of late initialization in init/attach
  lazy val sessionActor: ActorRef = _sessionActor

  /** actor for this UI */
  // implicit injects the `self` ActorRef as sender to `!` function of `ActorRef`
  // lazy because of late initialization in init/attach
  implicit lazy val self: ActorRef = _uiActor

  /** implement this instead of overriding [[init]] */
  // abstract, must be implemented, can't be forgotten
  def initVaactorsUI(request: ScaladinRequest): Unit

  /** override [[initVaactorsUI]] instead of this final function */
  final override def init(request: ScaladinRequest): Unit = {
    // attach ist not called, must do it in init()
    _sessionActor = ScaladinSession.current.getAttribute(classOf[ActorRef])
    _uiActor = VaactorsServlet.system.actorOf(Props(classOf[VaactorsUIActor], VaactorsUI.this))
    sessionActor ! VaactorsSession.SubscribeUI
    sessionActor ! VaactorsSession.RequestSession
    initVaactorsUI(request)
  }

  override def detach(): Unit = {
    sessionActor ! VaactorsSession.UnsubscribeUI
    self ! PoisonPill
    super.detach()
  }

  private def logUnprocessed: Actor.Receive = {
    case msg: Any =>
  }

  // lazy because receive is not yet initialized
  private lazy val receiveWorker = receive orElse logUnprocessed

  // forward message to receive function of ui, undefined messages are forwarded to logUnprocessed
  private[vaactor] def receiveMessage(msg: Any): Unit = access(receiveWorker(msg))

  def receive: Actor.Receive

}

private class VaactorsUIActor(ui: VaactorsUI) extends Actor {

  def receive = {
    // catch all messages and forward to UI
    case msg: Any =>
      ui.receiveMessage(msg)
  }

}
