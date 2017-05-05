package org.vaadin.addons.vaactor

import com.vaadin.server.VaadinSession

import akka.actor.{ ActorRef, PoisonPill, Props }

/** Toolbox for managing session actor within VaadinSession.
  *
  * Used by [[VaactorServlet]].
  *
  * Maybe useful for portlet implementation some day ...
  */
object VaactorVaadinSession {

  /** Type to be stored in VaadinSession.
    *
    * Is unique type.
    */
  private case class SessionActor(actor: Option[ActorRef])

  /** Store session actor in VaadinSession.
    *
    * @param session VaadinSession
    * @param actor   `Option[ActorRef]` to be stored
    */
  def storeSessionActor(session: VaadinSession, actor: Option[ActorRef]): Unit =
    session.setAttribute(classOf[SessionActor], SessionActor(actor))

  /** Create session actor and store it in VaadinSession.
    *
    * @param props   Props for creation oif session actor
    * @param session VaadinSession
    */
  def createAndStoreSessionActor(props: Option[Props], session: VaadinSession): Unit = {
    val actor = props map { VaactorSession.actorOf }
    storeSessionActor(session, actor)
  }

  /** Lookup session actor in VaadinSession
    *
    * @param session VaadinSession
    * @return `Option[ActorRef]` found in VaadinSession
    */
  def lookupSessionActor(session: VaadinSession): Option[ActorRef] =
    session.getAttribute(classOf[SessionActor]).actor

  /** Lookup session actor in VaadinSession and send a PoisonPill to it, if present.
    *
    * @param session VaadinSession
    */
  def lookupAndTerminateSessionActor(session: VaadinSession): Unit =
    lookupSessionActor(session) foreach { _ ! PoisonPill }

}
