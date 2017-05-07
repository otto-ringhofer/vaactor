package org.vaadin.addons.vaactor

import VaactorUI._
import com.typesafe.config.Config
import com.vaadin.server.VaadinSession
import com.vaadin.ui.UI

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, _ }

/** Creates ui guardian actor as supervisor for all ui actors.
  *
  * Contains ui-actor class instantiated for all VaactorUIs
  *
  * @author Otto Ringhofer
  */
object VaactorUI {

  /** UiGuardian, creates and supervises all ui actors */
  class UiGuardian extends Actor {

    private var uis: Int = 0

    def receive: PartialFunction[Any, Unit] = {
      case props: Props =>
        uis += 1
        val name = s"${ self.path.name }-${ props.actorClass.getSimpleName }-$uis"
        sender ! context.actorOf(props, name) // neuen Kind-Actor erzeugen
    }

  }

  /** `ui`-subtree of Vaactor configuration */
  val uiConfig: Config = config.getConfig("ui")

  /** [[UiGuardian]] actor, creates all ui-actors */
  val guardian: ActorRef = VaactorServlet.system.actorOf(
    Props[UiGuardian], uiConfig.getString("guardian-name"))

  import akka.pattern.ask
  import akka.util.Timeout

  private val askTimeout = Timeout(uiConfig.getInt("ask-timeout").seconds)

  /** create an actor as child of [[guardian]]
    *
    * @param props Props of acctor to be created
    * @return ActorRef of created actor
    */
  def actorOf(props: Props): ActorRef =
    Await.result((guardian ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)

  /** UiActor, creates and supervises all VaactorActors.
    *
    * Is instatiated once for each [[VaactorUI]].
    */
  class UiActor extends Actor {

    private var vaactors: Int = 0

    def receive: Receive = {
      case props: Props =>
        vaactors += 1
        val name = s"${ self.path.name }-${ props.actorClass.getSimpleName }-$vaactors"
        sender ! context.actorOf(props, name) // create new child actor
    }

  }

}

/** UI with actors
  *
  * contains guardian actor for all vaactor-actors
  * is also a vaactor
  *
  * @author Otto Ringhofer
  */
abstract class VaactorUI extends UI with Vaactor {

  /** Guardian actor, creates all vaactor-actors */
  val uiActor: ActorRef = VaactorUI.actorOf(Props(classOf[UiActor]))

  /** is ui of its own vaactor */
  val vaactorUI: VaactorUI = this

  // will be initialized in init/attach, not possible before
  private var _sessionActor: Option[ActorRef] = None

  /** Session actor for this UI */
  // lazy because of late initialization in init/attach
  lazy val sessionActor: Option[ActorRef] = _sessionActor

  /** Send a message to the session actor.
    *
    * No message is sent, if [[VaactorServlet.sessionProps]] is None
    */
  def send2SessionActor(msg: Any): Unit = sessionActor foreach { _ ! msg }

  override def attach(): Unit = {
    super.attach()
    _sessionActor = VaactorVaadinSession.lookupSessionActor(VaadinSession.getCurrent)
    send2SessionActor(VaactorSession.Subscribe)
    send2SessionActor(VaactorSession.RequestSessionState)
  }

  override def detach(): Unit = {
    send2SessionActor(VaactorSession.Unsubscribe)
    uiActor ! PoisonPill // stops also all vaactor children of this guardian
    super.detach()
  }

  import akka.pattern.ask

  /** Create an actor as child of [[uiActor]]
    *
    * @param props Props of acctor to be created
    * @return ActorRef of created actor
    */
  def actorOf(props: Props): ActorRef =
    Await.result((uiActor ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)

}
