package org.vaadin.addons.vaactor

import com.vaadin.server.VaadinSession

import akka.actor.{ Actor, ActorRef, PoisonPill, Props }

/** Toolbox for managing session actor within VaadinSession.
  *
  * Used by [[VaactorServlet]].
  *
  * Maybe useful for portlet implementation some day ...
  *
  * @author Otto Ringhofer
  */
object VaactorVaadinSession {

  /** Default session actor.
    *
    * Is used if sessionProps in VaactorServlet is None.
    *
    * Session type is None.type, session state is always None.
    * Session behaviour ignores all messages except those defined in VaactorSession.
    */
  class DefaultSessionActor extends Actor with VaactorSession[None.type] {

    override val initialSessionState: None.type = None

    override val sessionBehaviour: Receive = { case _ => }

  }

  /** The single instance of the default session actor, if no specific session actor is configured. */
  lazy val defaultSessionActor: ActorRef = VaactorSession.actorOf(Props[DefaultSessionActor])

  /** Type to be stored in VaadinSession.
    *
    * Is unique type.
    */
  private case class SessionActor(actor: ActorRef)

  /** Store session actor in VaadinSession.
    *
    * @param session VaadinSession
    * @param actor   `Option[ActorRef]` to be stored
    */
  def storeSessionActor(session: VaadinSession, actor: ActorRef): Unit =
    session.setAttribute(classOf[SessionActor], SessionActor(actor))

  /** Create session actor and store it in VaadinSession.
    *
    * If props is None, the single instance of [[DefaultSessionActor]] is used.
    *
    * @param props   Props for creation of session actor
    * @param session VaadinSession
    */
  def createAndStoreSessionActor(props: Option[Props], session: VaadinSession): Unit = {
    val actor = props match {
      case Some(p) => VaactorSession.actorOf(p)
      case None => defaultSessionActor
    }
    storeSessionActor(session, actor)
  }

  /** Lookup session actor in VaadinSession
    *
    * @param session VaadinSession
    * @return `Option[ActorRef]` found in VaadinSession
    */
  def lookupSessionActor(session: VaadinSession): ActorRef =
    session.getAttribute(classOf[SessionActor]).actor

  /** Lookup session actor in VaadinSession and send a PoisonPill to it, if present.
    *
    * If props is None, the single instance of [[DefaultSessionActor]] is NOT poisoned.
    *
    * @param session VaadinSession
    */
  def lookupAndTerminateSessionActor(props: Option[Props], session: VaadinSession): Unit =
    props foreach { _ => lookupSessionActor(session) ! PoisonPill }

}
