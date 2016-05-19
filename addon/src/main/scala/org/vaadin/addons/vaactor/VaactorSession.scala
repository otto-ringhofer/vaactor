package org.vaadin.addons.vaactor

import VaactorSession._

import akka.actor.{ Actor, ActorRef, Props }

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

object VaactorSession {

  val sessionConfig = vaactorConfig.getConfig("session")

  class Guardian extends Actor {
    private var sessions: Int = 0

    def receive = {
      case props: Props =>
        sessions += 1
        val name = s"${ self.path.name }-${ props.actorClass.getSimpleName }-$sessions"
        sender ! context.actorOf(props, name) // neuen Kind-Actor erzeugen
    }
  }

  /** send current session to sender */
  case object RequestSession

  /** send current session to all registered ui-actors */
  case object BroadcastSession

  /** add sender to uiActorSet */
  case object SubscribeUI

  /** remove sender from uiActorSet */
  case object UnsubscribeUI

  val guardian = VaactorServlet.system.actorOf(
    Props[Guardian], sessionConfig.getString("guardian-name"))

  import akka.pattern.ask
  import akka.util.Timeout

  private val askTimeout = Timeout(sessionConfig.getInt("ask-timeout").seconds)

  def actorOf(props: Props): ActorRef = {
    Await.result((guardian ? props) (askTimeout).mapTo[ActorRef], Duration.Inf)
  }

}

trait VaactorSession[S] {
  this: Actor =>

  private[vaactor] val uiActors = mutable.Set.empty[ActorRef]

  private var _session = initialSession

  /** returns initial value of session */
  def initialSession: S

  /** returns current session */
  def session: S = _session

  /** sets current session */
  def session_=(s: S): Unit = _session = s

  /** behaviour handles all messages defined in [[VaactorSession]] */
  val sessionBehaviour: Receive = {
    case RequestSession =>
      sender ! session
    case BroadcastSession =>
      broadcast(session)
    case SubscribeUI =>
      uiActors += sender
    case UnsubscribeUI =>
      uiActors -= sender
  }

  def broadcast(msg: Any): Unit = for (ui <- uiActors) ui ! msg

}
