package org.vaadin.addons.vaactor

import VaactorVaadinSession._
import com.vaadin.flow.server.{ SessionDestroyEvent, SessionDestroyListener, SessionInitEvent, SessionInitListener }

import akka.actor.Props

/** Servlet creates and destroys session Actors
  *
  * @author Otto Ringhofer
  */
abstract class VaactorSessionServlet extends VaactorServlet
  with SessionInitListener with SessionDestroyListener {

  /** Props for creating session Actors for this service */
  val sessionProps: Props

  /** Register init and destroy listeners */
  override protected def servletInitialized(): Unit = {
    super.servletInitialized()
    getService.addSessionInitListener(VaactorSessionServlet.this)
    getService.addSessionDestroyListener(VaactorSessionServlet.this)
  }

  /** Create session Actor, store it in vaadin-session */
  override def sessionInit(event: SessionInitEvent): Unit =
    createAndStoreSessionActor(event.getSession, sessionProps)

  /** Stop session Actor */
  override def sessionDestroy(event: SessionDestroyEvent): Unit =
    lookupAndTerminateSessionActor(event.getSession)

}
