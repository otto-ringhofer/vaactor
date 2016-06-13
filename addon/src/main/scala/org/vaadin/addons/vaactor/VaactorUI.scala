package org.vaadin.addons.vaactor

import VaactorUI._

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }
import vaadin.scala.server.{ ScaladinRequest, ScaladinSession }
import vaadin.scala.{ PushMode, UI }

import scala.concurrent.Await
import scala.concurrent.duration._

object VaactorUI {

  val uiConfig = vaactorConfig.getConfig("ui")

  class Guardian extends Actor {

    private var uis: Int = 0

    def receive = {
      case props: Props =>
        uis += 1
        val name = s"${ self.path.name }-${ props.actorClass.getSimpleName }-$uis"
        sender ! context.actorOf(props, name) // neuen Kind-Actor erzeugen
    }

  }

  val guardian = VaactorServlet.system.actorOf(
    Props[Guardian], uiConfig.getString("guardian-name"))

  import akka.pattern.ask
  import akka.util.Timeout

  private val askTimeout = Timeout(uiConfig.getInt("ask-timeout").seconds)

  def actorOf(props: Props): ActorRef =
    Await.result((guardian ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)

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
  extends UI(title, theme, widgetset, preserveOnRefresh, pushMode) {
  vaactorUI =>

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
  def initVaactorUI(request: ScaladinRequest): Unit

  /** override [[initVaactorUI]] instead of this final function */
  final override def init(request: ScaladinRequest): Unit = {
    // attach ist not called, must do it in init()
    _sessionActor = ScaladinSession.current.getAttribute(classOf[ActorRef])
    _uiActor = VaactorUI.actorOf(Props(classOf[VaactorUIActor], vaactorUI))
    sessionActor ! VaactorSession.SubscribeUI
    sessionActor ! VaactorSession.RequestSession
    initVaactorUI(request)
  }

  override def detach(): Unit = {
    sessionActor ! VaactorSession.UnsubscribeUI
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

private class VaactorUIActor(ui: VaactorUI) extends Actor {

  def receive = {
    // catch all messages and forward to UI
    case msg: Any =>
      ui.receiveMessage(msg)
  }

}
