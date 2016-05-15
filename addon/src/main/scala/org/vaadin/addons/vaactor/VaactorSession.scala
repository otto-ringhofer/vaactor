package org.vaadin.addons.vaactor

import VaactorsSession._

import akka.actor.{ Actor, ActorRef }

import scala.collection.mutable

object VaactorsSession {

  /** send current session to sender */
  case object RequestSession

  /** send current session to all registered ui-actors */
  case object BroadcastSession

  /** add sender to uiActorSet */
  case object SubscribeUI

  /** remove sender from uiActorSet */
  case object UnsubscribeUI

}

trait VaactorsSession[S] {
  this: Actor =>

  private val uiActors = mutable.Set.empty[ActorRef]

  /** returns current session */
  def session: S

  /** behaviour handles all messages defined in [[VaactorsSession]] */
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
