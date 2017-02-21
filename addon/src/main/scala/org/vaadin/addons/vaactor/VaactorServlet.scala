package org.vaadin.addons.vaactor

import javax.servlet.ServletConfig

import VaactorServlet._
import com.typesafe.config.Config
import com.vaadin.server.{ SessionDestroyEvent, SessionDestroyListener, SessionInitEvent, SessionInitListener, VaadinServlet }

import akka.actor.{ ActorRef, ActorSystem, PoisonPill, Props }

/** Initializes and stores the actor system
  *
  * @author Otto Ringhofer
  */
object VaactorServlet {

  /** the actor system */
  val system: ActorSystem = ActorSystem(config.getString("system-name"))
  val servletConfig: Config = config.getConfig("servlet")
}

/** servlet creates and destroys session actors
  *
  * @param ui                UI class to be instantiated
  * @param productionMode    default from configuration `production-mode`
  * @param widgetset         default from configuration `widgetset`
  * @param resourceCacheTime default from configuration `resource-cache-time`
  * @param heartbeatInterval default from configuration `heartbeat-interval`
  * @param closeIdleSessions default from configuration `close-idle-sessions`
  * @author Otto Ringhofer
  */
// TODO handle params
abstract class VaactorServlet(
  ui: Class[_],
  productionMode: Boolean = servletConfig.getBoolean("production-mode"),
  widgetset: String = servletConfig.getString("widgetset"),
  resourceCacheTime: Int = servletConfig.getInt("resource-cache-time"),
  heartbeatInterval: Int = servletConfig.getInt("heartbeat-interval"),
  closeIdleSessions: Boolean = servletConfig.getBoolean("close-idle-sessions")
) extends VaadinServlet
  with SessionInitListener with SessionDestroyListener {

  /** Props for creating a session actor for this service */
  val sessionProps: Props

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
  override def sessionInit(event: SessionInitEvent): Unit = {
    val actor = VaactorSession.actorOf(sessionProps)
    event.getSession.setAttribute(classOf[ActorRef], actor)
  }

  /** stop session actor */
  override def sessionDestroy(event: SessionDestroyEvent): Unit = {
    val actor = event.getSession.getAttribute(classOf[ActorRef])
    if (actor != null) actor ! PoisonPill
  }

}
