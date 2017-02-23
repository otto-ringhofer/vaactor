package org.vaadin.addons.vaactor

import VaactorSession._
import com.typesafe.config.Config

import akka.actor.{ Actor, ActorRef, Props, Stash }

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

/** contains guardian actor for all session-actors
  *
  * @author Otto Ringhofer
  */
object VaactorSession {

  val sessionConfig: Config = config.getConfig("session")

  class Guardian extends Actor {

    private var sessions: Int = 0

    def receive: PartialFunction[Any, Unit] = {
      case props: Props =>
        sessions += 1
        val name = s"${ self.path.name }-${ props.actorClass.getSimpleName }-$sessions"
        sender ! context.actorOf(props, name) // neuen Kind-Actor erzeugen
    }

  }

  /** send current session state to sender */
  case object RequestSessionState

  /** send current session state to all registered ui-actors */
  case object BroadcastSessionState

  /** add sender to uiActorSet */
  case object SubscribeUI

  /** remove sender from uiActorSet */
  case object UnsubscribeUI

  /** guardian actor, creates all session-actors */
  val guardian: ActorRef = VaactorServlet.system.actorOf(
    Props[Guardian], sessionConfig.getString("guardian-name"))

  import akka.pattern.ask
  import akka.util.Timeout

  private val askTimeout = Timeout(sessionConfig.getInt("ask-timeout").seconds)

  /** create an actor as child of [[guardian]]
    *
    * @param props Props of acctor to be created
    * @return ActorRef of created actor
    */
  def actorOf(props: Props): ActorRef =
    Await.result((guardian ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)

}

/** helper trait for session actors
  *
  * handles session state variable
  * handles messages for management of ui-actors in session
  * keeps state during restart
  *
  * @tparam S type of session state
  * @author Otto Ringhofer
  */
trait VaactorSession[S] extends Stash {
  this: Actor =>

  private case class InitialSessionState(session: S)

  /** returns initial value of session state */
  val initialSessionState: S

  /** defines behaviour of session actor */
  val sessionBehaviour: Receive

  private[vaactor] val uiActors = mutable.Set.empty[ActorRef]

  private var _sessionState = initialSessionState

  /** returns current session state */
  def sessionState: S = _sessionState

  /** sets current session state */
  def sessionState_=(s: S): Unit = _sessionState = s

  /** send message to all ui-actors in this session
    *
    * @param msg message to be sent
    */
  def broadcast(msg: Any): Unit = for (ui <- uiActors) ui ! msg

  /** initialize session state, will switch behaviour */
  override def preStart(): Unit = {
    self ! InitialSessionState(initialSessionState)
  }

  /** sends current session state to next instance after restart, will switch behaviour */
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    self ! InitialSessionState(sessionState)
  }

  /** inhibits call to preStart during restart */
  override def postRestart(reason: Throwable): Unit = {}

  /** behaviour handles all messages defined in [[VaactorSession]] */
  val vaactorSessionBehaviour: Receive = {
    case RequestSessionState =>
      sender ! sessionState
    case BroadcastSessionState =>
      broadcast(sessionState)
    case SubscribeUI =>
      uiActors += sender
    case UnsubscribeUI =>
      uiActors -= sender
  }

  /** initial behaviour, receives initial session state */
  final val receive: Receive = {
    case InitialSessionState(s) =>
      sessionState = s
      context.become(sessionBehaviour orElse vaactorSessionBehaviour)
      unstashAll()
    case _ =>
      stash()
  }

}
