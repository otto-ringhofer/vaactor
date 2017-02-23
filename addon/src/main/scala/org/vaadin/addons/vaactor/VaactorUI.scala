package org.vaadin.addons.vaactor

import VaactorUI._
import com.typesafe.config.Config
import com.vaadin.server.{ VaadinRequest, VaadinSession }
import com.vaadin.ui.UI

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }

import scala.concurrent.Await
import scala.concurrent.duration.{ Duration, _ }

/** contains guardian class for all vaactor-actors
  *
  * @author Otto Ringhofer
  */
object VaactorUI {

  val uiConfig: Config = config.getConfig("ui")

  class UiGuardian extends Actor {

    private var vaactors: Int = 0

    def receive: PartialFunction[Any, Unit] = {
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
  * @author Otto Ringhofer
  */
abstract class VaactorUI extends UI with Vaactor {

  /** guardian actor, creates all vaactor-actors */
  val uiGuardian: ActorRef = Vaactor.actorOf(Props(classOf[UiGuardian]))

  /** is ui of its own vaactor */
  val vaactorUI: VaactorUI = this

  // will be initialized in init/attach, not possible before
  private var _sessionActor: Option[ActorRef] = None

  /** session actor for this UI */
  // lazy because of late initialization in init/attach
  lazy val sessionActor: Option[ActorRef] = _sessionActor

  /** Send a message to the session actor.
    *
    * No message is sent, if [[VaactorServlet.sessionProps]] is None
    */
  // lazy because of late initialization in init/attach
  lazy val send2SessionActor: Any => Unit = sessionActor match {
    case Some(actor) => actor ! _
    case None => _ => {}
  }

  /** implement this instead of overriding [[init]] */
  // abstract, must be implemented, can't be forgotten
  def initVaactorUI(request: VaadinRequest): Unit

  /** override [[initVaactorUI]] instead of this final function */
  final override def init(request: VaadinRequest): Unit = {
    // attach ist not called, must do it in init()
    _sessionActor = VaadinSession.getCurrent.getAttribute(classOf[Option[ActorRef]])
    send2SessionActor(VaactorSession.SubscribeUI)
    send2SessionActor(VaactorSession.RequestSession)
    initVaactorUI(request)
  }

  override def detach(): Unit = {
    send2SessionActor(VaactorSession.UnsubscribeUI)
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
