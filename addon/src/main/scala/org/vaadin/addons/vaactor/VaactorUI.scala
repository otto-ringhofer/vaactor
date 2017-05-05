package org.vaadin.addons.vaactor

import VaactorUI._
import com.typesafe.config.Config
import com.vaadin.server.VaadinSession
import com.vaadin.ui.UI

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, _ }

/** Contains guardian class instantiated for all VaactorUIs
  *
  * @author Otto Ringhofer
  */
object VaactorUI {

  val uiConfig: Config = config.getConfig("ui")

  /** UI Guardian actor.
    *
    * Creates and supervises all VaactorActors.
    * Is instatiated for each [[VaactorUI]].
    */
  class UiGuardian extends Actor {

    private var vaactors: Int = 0

    def receive: Receive = {
      case props: Props =>
        vaactors += 1
        val name = s"${ self.path.name }-${ props.actorClass.getSimpleName }-$vaactors"
        sender ! context.actorOf(props, name) // create new child actor
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
  * @author Otto Ringhofer
  */
abstract class VaactorUI extends UI with Vaactor {

  /** Guardian actor, creates all vaactor-actors */
  val uiGuardian: ActorRef = Vaactor.actorOf(Props(classOf[UiGuardian]))

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
    uiGuardian ! PoisonPill // stops also all vaactor children of this guardian
    super.detach()
  }

  import akka.pattern.ask

  /** Create an actor as child of [[uiGuardian]]
    *
    * @param props Props of acctor to be created
    * @return ActorRef of created actor
    */
  def actorOf(props: Props): ActorRef =
    Await.result((uiGuardian ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)

}
