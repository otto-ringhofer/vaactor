package org.vaadin.addons.vaactor

import VaactorUI._

import akka.actor.{ ActorRef, PoisonPill }
import vaadin.scala.server.{ ScaladinRequest, ScaladinSession }
import vaadin.scala.{ PushMode, UI }

object VaactorUI {

  val uiConfig = config.getConfig("ui")

}

/** UI with actors */
abstract class VaactorUI(
  title: String = null,
  theme: String = null,
  widgetset: String = null,
  preserveOnRefresh: Boolean = uiConfig.getBoolean("preserve-on-refresh"),
  pushMode: PushMode.Value = uiConfig.getString("push-mode") match {
    case "automatic" => PushMode.Automatic
    case "manual" => PushMode.Manual
  })
  extends UI(title, theme, widgetset, preserveOnRefresh, pushMode)
    with Vaactor {

  lazy val vaactorUI = this

  // will be initialized in init, not possible before
  private var _sessionActor: ActorRef = _

  /** session actor for this UI */
  // lazy because of late initialization in init/attach
  lazy val sessionActor: ActorRef = _sessionActor

  /** implement this instead of overriding [[init]] */
  // abstract, must be implemented, can't be forgotten
  def initVaactorUI(request: ScaladinRequest): Unit

  /** override [[initVaactorUI]] instead of this final function */
  final override def init(request: ScaladinRequest): Unit = {
    // attach ist not called, must do it in init()
    _sessionActor = ScaladinSession.current.getAttribute(classOf[ActorRef])
    sessionActor ! VaactorSession.SubscribeUI
    sessionActor ! VaactorSession.RequestSession
    initVaactorUI(request)
  }

  override def detach(): Unit = {
    sessionActor ! VaactorSession.UnsubscribeUI
    self ! PoisonPill
    super.detach()
  }

}
