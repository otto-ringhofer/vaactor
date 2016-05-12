package org.vaadin.addons.vaactors

import javax.servlet.ServletConfig

import VaactorsServlet._
import com.vaadin.server.{ SessionDestroyEvent, SessionDestroyListener, SessionInitEvent, SessionInitListener }

import akka.actor.{ ActorRef, ActorSystem, PoisonPill, Props }
import vaadin.scala.server.ScaladinServlet

/** Initializes and stores the actor system */
object VaactorsServlet {

  /** the actor system */
  val system: ActorSystem = ActorSystem("vaactors-servlet")

}

abstract class VaactorsServlet(
  ui: Class[_],
  productionMode: Boolean = true,
  widgetset: String = "com.vaadin.DefaultWidgetSet",
  resourceCacheTime: Int = 3600,
  heartbeatInterval: Int = 300,
  closeIdleSessions: Boolean = true
) extends ScaladinServlet(
  ui, productionMode, widgetset, resourceCacheTime, heartbeatInterval, closeIdleSessions)
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
    getService.addSessionInitListener(VaactorsServlet.this)
    getService.addSessionDestroyListener(VaactorsServlet.this)
  }

  /** create session actor */
  override def sessionInit(event: SessionInitEvent): Unit = {
    val actor = system.actorOf(sessionProps)
    event.getSession.setAttribute(classOf[ActorRef], actor)
  }

  /** stop session actor */
  override def sessionDestroy(event: SessionDestroyEvent): Unit = {
    val actor = event.getSession.getAttribute(classOf[ActorRef])
    if (actor != null) actor ! PoisonPill
  }

}
