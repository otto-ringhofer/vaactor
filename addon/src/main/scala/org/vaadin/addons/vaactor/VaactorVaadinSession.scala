package org.vaadin.addons.vaactor


import org.vaadin.addons.vaactor.Vaactor.SessionWithoutActorException
import com.vaadin.flow.server.VaadinSession

import akka.actor.{ ActorRef, PoisonPill, Props }

import scala.util.{ Failure, Success, Try }

/** Toolbox for managing session Actor within VaadinSession.
  *
  * Used by [[VaactorSessionServlet]].
  *
  * Maybe useful for portlet implementation some day ...
  *
  * @author Otto Ringhofer
  */
private[vaactor] object VaactorVaadinSession {

  /** Type to be stored in VaadinSession.
    *
    * Is unique type.
    */
  private case class SessionActor(actor: ActorRef)

  /** Store session Actor in VaadinSession.
    *
    * @param session VaadinSession
    * @param actor   ActorRef to be stored
    */
  def storeSessionActor(session: VaadinSession, actor: ActorRef): Unit =
    session.setAttribute(classOf[SessionActor], SessionActor(actor))

  /** Create session Actor and store it in VaadinSession.
    *
    * @param session VaadinSession
    * @param props   Props for creation of session Actor
    */
  def createAndStoreSessionActor(session: VaadinSession, props: Props): Unit =
    storeSessionActor(session, VaactorSession.actorOf(props))

  /** Lookup session Actor in VaadinSession
    *
    * @param session VaadinSession
    * @return `Try[ActorRef]` found in VaadinSession
    */
  def lookupSessionActor(session: VaadinSession): Try[ActorRef] =
    Option(session.getAttribute(classOf[SessionActor])) match {
      case Some(sessionActor) => Success(sessionActor.actor)
      case None => Failure(SessionWithoutActorException)
    }

  /** Lookup session Actor in VaadinSession and send a PoisonPill to it, if present.
    *
    * @param session VaadinSession
    */
  def lookupAndTerminateSessionActor(session: VaadinSession): Unit =
    for (sessionActor <- lookupSessionActor(session)) sessionActor ! PoisonPill

}
