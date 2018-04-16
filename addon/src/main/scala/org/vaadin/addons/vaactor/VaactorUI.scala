package org.vaadin.addons.vaactor

import VaactorUI._
import org.vaadin.addons.vaactor.VaactorSession.{ Broadcast, Subscribe, Unsubscribe }
import com.typesafe.config.Config
import com.vaadin.flow.component.{ AttachEvent, Component, Composite, DetachEvent, UI }
import com.vaadin.flow.server.VaadinSession

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

    def receive: Receive = {
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

  /** Create an actor as child of [[guardian]]
    *
    * @param props Props of acctor to be created
    * @return ActorRef of created actor
    */
  def actorOf(props: Props): ActorRef =
    Await.result((guardian ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)

  /** UiActor, creates and supervises all VaactorActors.
    *
    * Handles messages for management of subscribers in ui.
    *
    * Is instatiated once for each [[VaactorUI]].
    */
  class UiActor extends Actor {

    private[vaactor] var subscribers = Set.empty[ActorRef]

    private var vaactors: Int = 0

    def receive: Receive = {
      case Broadcast(msg) =>
        for (subscriber <- subscribers) subscriber.tell(msg, sender)
      case Subscribe =>
        subscribers += sender
      case Subscribe(s) =>
        subscribers += s
      case Unsubscribe =>
        subscribers -= sender
      case Unsubscribe(s) =>
        subscribers -= s
      case props: Props =>
        vaactors += 1
        val name = s"${ self.path.name }-${ props.actorClass.getSimpleName }-$vaactors"
        sender ! context.actorOf(props, name) // create new child actor
    }

  }

}

/** UI with actors
  *
  * Contains guardian actor for all vaactor-actors
  *
  * @author Otto Ringhofer
  */
abstract class VaactorUI extends Composite[Component] {

  /** Guardian actor, creates all vaactor-actors */
  val uiActor: ActorRef = VaactorUI.actorOf(Props(classOf[UiActor]))

  // will be initialized in init/attach, not possible before
  private var _sessionActor: ActorRef = _

  // will be initialized in attach, not possible before
  private var _vaadinUI: Option[UI] = None

  /** Session actor for this UI */
  // lazy because of late initialization in init/attach
  lazy val sessionActor: ActorRef = _sessionActor

  /** Vaadin UI für dieses SVUI
    *
    * Darf erst nach dem Initialisieren des SVUI verwendet werden!!!
    *
    * UI Klassen sollten daher die eigene Referenz erst in der eigenen init() Methode weitergeben.
    * Das wird u.a. dadurch erreicht, dass weitere Komponenten,
    * an deren Konstruktor die eigene Referenz weitergegeben wird,
    * erst in der init() Methode erzeugt werden.
    */
  // lazy because of late initialization in attach
  lazy val vaadinUI: UI = _vaadinUI.get

  /** Send a message to the session actor.
    *
    * No message is sent, if [[VaactorServlet.sessionProps]] is None
    *
    * @param msg    message to be sent
    * @param sender sender of message
    */
  def send2SessionActor(msg: Any, sender: ActorRef = Actor.noSender): Unit =
    sessionActor.tell(msg, sender)

  override def onAttach(attachEvent: AttachEvent): Unit = {
    super.onAttach(attachEvent)
    _sessionActor = VaactorVaadinSession.lookupSessionActor(VaadinSession.getCurrent)
    _vaadinUI = Some(UI.getCurrent)
  }

  override def onDetach(detachEvent: DetachEvent): Unit = {
    uiActor ! PoisonPill // stops also all vaactor children of this guardian
    super.onDetach(detachEvent)
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
