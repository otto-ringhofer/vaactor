package org.vaadin.addons.vaactor

import VaactorUI._

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }
import vaadin.scala.server.{ ScaladinRequest, ScaladinSession }
import vaadin.scala.{ PushMode, UI }

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, _ }

object VaactorUI {

  val uiConfig = config.getConfig("ui")

  class UiGuardian extends Actor {

    private var vaactors: Int = 0

    def receive = {
      case props: Props =>
        vaactors += 1
        val name = s"${ self.path.name }-${ props.actorClass.getSimpleName }-$vaactors"
        sender ! context.actorOf(props, name) // neuen Kind-Actor erzeugen
    }

  }

  import akka.util.Timeout

  val askTimeout = Timeout(uiConfig.getInt("ask-timeout").seconds)


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

  val uiGuardian = Vaactor.actorOf(Props(classOf[UiGuardian]))

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
    uiGuardian ! PoisonPill // stops also all vaactor children of this guardian
    super.detach()
  }

  import akka.pattern.ask

  def actorOf(props: Props): ActorRef =
    Await.result((uiGuardian ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)

}
