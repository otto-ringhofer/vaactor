package org.vaadin.addons.vaactor

import VaactorUI._
import com.vaadin.server.{ VaadinRequest, VaadinSession }
import com.vaadin.shared.communication.PushMode
import com.vaadin.ui.UI

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, _ }

/** contains guardian class for all vaactor-actors
  *
  * @author Otto Ringhofer
  */
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

/** UI with actors
  *
  * contains guardian actor for all vaactor-actors
  * is also a vaactor
  *
  * @param title             title of ui, default null
  * @param theme             theme of ui, default null
  * @param widgetset         widgetset of ui, default from servlet
  * @param preserveOnRefresh default from configuration `preserve-on-refresh`
  * @param pushMode          default from configuration `push-mode`
  * @author Otto Ringhofer
  */
abstract class VaactorUI(
  title: String = null,
  theme: String = null,
  widgetset: String = null,
  preserveOnRefresh: Boolean = uiConfig.getBoolean("preserve-on-refresh"),
  pushMode: PushMode = uiConfig.getString("push-mode") match {
    case "automatic" => PushMode.AUTOMATIC
    case "manual" => PushMode.MANUAL
  })
// TODO handle params
  extends UI with Vaactor {

  /** guardian actor, creates all vaactor-actors */
  // lazy because of DelayedInit from UI - TODO remove after removed in UI
  lazy val uiGuardian = Vaactor.actorOf(Props(classOf[UiGuardian]))

  /** is ui of its own vaactor */
  lazy val vaactorUI = this

  // will be initialized in init, not possible before
  private var _sessionActor: ActorRef = _

  /** session actor for this UI */
  // lazy because of late initialization in init/attach
  lazy val sessionActor: ActorRef = _sessionActor

  /** implement this instead of overriding [[init]] */
  // abstract, must be implemented, can't be forgotten
  def initVaactorUI(request: VaadinRequest): Unit

  /** override [[initVaactorUI]] instead of this final function */
  final override def init(request: VaadinRequest): Unit = {
    // attach ist not called, must do it in init()
    _sessionActor = VaadinSession.getCurrent.getAttribute(classOf[ActorRef])
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

  /** create an actor as child of [[uiGuardian]]
    *
    * @param props Props of acctor to be created
    * @return ActorRef of created actor
    */
  def actorOf(props: Props): ActorRef =
    Await.result((uiGuardian ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)

}
