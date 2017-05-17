package org.vaadin.addons.vaactor

import javax.servlet.ServletConfig

import VaactorServlet._
import VaactorVaadinSession._
import com.vaadin.server.{ SessionDestroyEvent, SessionDestroyListener, SessionInitEvent, SessionInitListener, VaadinServlet }

import akka.actor.{ ActorSystem, Props }

/** Initializes and stores the actor system
  *
  * @author Otto Ringhofer
  */
object VaactorServlet {

  /** the actor system */
  val system: ActorSystem = ActorSystem(
    config.getString("system-name"),
    config.withFallback(loadedConfig)
  )

}

/** servlet creates and destroys session actors
  *
  * @author Otto Ringhofer
  */
abstract class VaactorServlet extends VaadinServlet
  with SessionInitListener with SessionDestroyListener {

  /** Props for creating a session actor for this service */
  val sessionProps: Option[Props] = None

  /** log init */
  override def init(servletConfig: ServletConfig): Unit = {
    super.init(servletConfig)
  }

  /** terminate actor system, log destroy */
  override def destroy(): Unit = {
    super.destroy()
    system.terminate()
  }

  /** register init and destroy listeners */
  override protected def servletInitialized(): Unit = {
    super.servletInitialized()
    getService.addSessionInitListener(VaactorServlet.this)
    getService.addSessionDestroyListener(VaactorServlet.this)
  }

  /** create session actor, store it in vaadin-session */
  override def sessionInit(event: SessionInitEvent): Unit =
    createAndStoreSessionActor(sessionProps, event.getSession)

  /** stop session actor */
  override def sessionDestroy(event: SessionDestroyEvent): Unit =
    lookupAndTerminateSessionActor(sessionProps, event.getSession)

}
