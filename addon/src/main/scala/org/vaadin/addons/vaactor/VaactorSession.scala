package org.vaadin.addons.vaactor

import VaactorSession._
import com.typesafe.config.Config

import akka.actor.{ Actor, ActorRef, Props, Stash }

import scala.concurrent.Await
import scala.concurrent.duration._

/** Vaadin session management with Actors.
  *
  * Creates session guardian Actor as supervisor for all session Actors.
  *
  * Defines messages used by trait [[VaactorSession]].
  *
  * @author Otto Ringhofer
  */
object VaactorSession {

  protected case class InitialSessionState[S](session: S, subscribers: Set[ActorRef])

  /** SessionGuardian, creates and supervises all session Actors */
  class SessionGuardian extends Actor {

    private var sessions: Int = 0

    def receive: Receive = {
      case props: Props =>
        sessions += 1
        val name = s"${ self.path.name }-${ props.actorClass.getSimpleName }-$sessions"
        sender ! context.actorOf(props, name) // create new child Actor
    }

  }

  /** `session`-subtree of Vaactor configuration */
  val sessionConfig: Config = config.getConfig("session")

  /** Marker trait for all messages processed by [[VaactorSession.vaactorSessionBehaviour]].
    *
    * All messages implementing this sealed trait are defined in object [[VaactorSession]].
    */
  sealed trait VaactorSessionMessage

  /** Send current session state to sender */
  case object RequestSessionState extends VaactorSessionMessage

  /** Send current session state to all registered subscribers */
  case object BroadcastSessionState extends VaactorSessionMessage

  /** Send message to all registered subscribers
    *
    * @param msg message to be sent
    * @tparam T type of message
    */
  case class Broadcast[T](msg: T) extends VaactorSessionMessage

  /** Register sender as subscriber */
  case object Subscribe extends VaactorSessionMessage

  /** Register Actor as subscriber
    *
    * @param actor ActorRef of subcriber
    */
  case class Subscribe(actor: ActorRef) extends VaactorSessionMessage

  /** Remove sender from list of subscribers */
  case object Unsubscribe extends VaactorSessionMessage

  /** Remove Actor from list of subscribers
    *
    * @param actor ActorRef of subscriber to be removed
    */
  case class Unsubscribe(actor: ActorRef) extends VaactorSessionMessage

  /** Send [[WithSession]] message to receiver
    *
    * @param msg      message to be wrapped in WithSession
    * @param receiver desired receiver of WithSession message
    * @tparam T type of message
    */
  case class ForwardWithSession[T](msg: T, receiver: ActorRef) extends VaactorSessionMessage

  /** Send [[WithSession]] message to all registered subscribers
    *
    * @param msg message to be wrapped in WithSession
    * @tparam T type of message
    */
  case class BroadcastWithSession[T](msg: T) extends VaactorSessionMessage

  /** Wrap message and session state.
    * Is sent by session Actor on request.
    *
    * @param session current session state
    * @param msg     message
    * @tparam S type of session state
    * @tparam T type of message
    */
  case class WithSession[S, T](session: S, msg: T)

  /** [[SessionGuardian]] Actor, creates all session Actors */
  val guardian: ActorRef = VaactorServlet.system.actorOf(
    Props[SessionGuardian], sessionConfig.getString("guardian-name"))

  import akka.pattern.ask
  import akka.util.Timeout

  private val askTimeout = Timeout(sessionConfig.getInt("ask-timeout").seconds)

  /** Create an Actor as child of [[guardian]]
    *
    * @param props Props of acctor to be created
    * @return ActorRef of created Actor
    */
  def actorOf(props: Props): ActorRef =
    Await.result((guardian ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)

}

/** Helper trait for session Actors
  *
  * Handles session state variable.
  *
  * Handles messages for management of subscribers in session.
  *
  * Preserves session state and subscribers during restart.
  *
  * @tparam S type of session state
  * @author Otto Ringhofer
  */
trait VaactorSession[S] extends Stash {
  this: Actor =>

  /** Defines initial session state, ist set after creation of Actor.
    *
    * Is NOT set after restart of Actor.
    */
  val initialSessionState: S

  /** Handles all messages not handled by this trait */
  val sessionBehaviour: Receive

  private[vaactor] var subscribers = Set.empty[ActorRef]

  private var _sessionState = initialSessionState

  /** Return current session state */
  def sessionState: S = _sessionState

  /** Set current session state */
  def sessionState_=(s: S): Unit = _sessionState = s

  /** Send message to all subscribers in this session
    *
    * @param msg    message to be sent
    * @param sender sender of message
    * @tparam T type of message
    */
  def broadcast[T](msg: T, sender: ActorRef = self): Unit =
    for (subscriber <- subscribers) subscriber.tell(msg, sender)

  /** Initialize session state, will switch behaviour */
  override def preStart(): Unit = {
    self ! InitialSessionState(initialSessionState, Set.empty[ActorRef])
  }

  /** Send current session state to next instance after restart, will switch behaviour */
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    self ! InitialSessionState(sessionState, subscribers)
  }

  /** Inhibit call to preStart during restart */
  override def postRestart(reason: Throwable): Unit = {}

  /** Handle all messages marked by [[VaactorSession.VaactorSessionMessage]] */
  val vaactorSessionBehaviour: Receive = {
    case vaactorSessionMessage: VaactorSessionMessage => vaactorSessionMessage match {
      case RequestSessionState =>
        sender.forward(sessionState)
      case ForwardWithSession(msg, receiver) =>
        receiver.forward(WithSession(sessionState, msg))
      case BroadcastSessionState =>
        broadcast(sessionState, sender)
      case Broadcast(msg) =>
        broadcast(msg, sender)
      case BroadcastWithSession(msg) =>
        broadcast(WithSession(sessionState, msg), sender)
      case Subscribe =>
        subscribers += sender
      case Subscribe(s) =>
        subscribers += s
      case Unsubscribe =>
        subscribers -= sender
      case Unsubscribe(s) =>
        subscribers -= s
    }
  }

  /** Initial behaviour, waits for [[VaactorSession.InitialSessionState]] message */
  final val receive: Receive = {
    case InitialSessionState(state, subs) =>
      sessionState = state.asInstanceOf[S]
      subscribers = subs
      context.become(vaactorSessionBehaviour orElse sessionBehaviour)
      unstashAll()
    case _ =>
      stash()
  }

}
