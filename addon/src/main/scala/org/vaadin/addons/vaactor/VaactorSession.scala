package org.vaadin.addons.vaactor

import VaactorSession._

import akka.actor.{ Actor, ActorRef }

import scala.collection.mutable

object VaactorSession {

  /** send current session to sender */
  case object RequestSession

  /** send current session to all registered ui-actors */
  case object BroadcastSession

  /** add sender to uiActorSet */
  case object SubscribeUI

  /** remove sender from uiActorSet */
  case object UnsubscribeUI

}

trait VaactorSession[S] {
  this: Actor =>

  private[vaactor] val uiActors = mutable.Set.empty[ActorRef]

  /** returns current session */
  def session: S

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
