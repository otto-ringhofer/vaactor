package org.vaadin.addons.vaactor

import javax.servlet.ServletConfig

import com.vaadin.flow.server.VaadinServlet

import akka.actor.ActorSystem

object VaactorServlet {

  /** the actor system */
  val system: ActorSystem = ActorSystem(
    config.getString("system-name"),
    config.withFallback(loadedConfig)
  )

  private def dummyInit(): Unit = {}

}

/** Servlet creates and destroys ActorSystem
  *
  * @author Otto Ringhofer
  */
abstract class VaactorServlet extends VaadinServlet {

  /** Log init */
  override def init(servletConfig: ServletConfig): Unit = {
    super.init(servletConfig)
    VaactorServlet.dummyInit()
  }

  /** Terminate actor system */
  override def destroy(): Unit = {
    super.destroy()
    VaactorServlet.system.terminate()
  }

}
