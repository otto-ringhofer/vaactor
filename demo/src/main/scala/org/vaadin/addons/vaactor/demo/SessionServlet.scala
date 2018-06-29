package org.vaadin.addons.vaactor.demo

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactor.VaactorSessionServlet
import com.vaadin.flow.server.VaadinServletConfiguration

import akka.actor.Props

/** Define servlet, url pattern and ui-class to start
  *
  * @author Otto Ringhofer
  */
@WebServlet(
  urlPatterns = Array("/*"),
  asyncSupported = true)
@VaadinServletConfiguration(
  productionMode = false
)
class SessionServlet extends VaactorSessionServlet {

  /** Define session actor to be created for every session */
  override val sessionProps = Props(classOf[Session.SessionActor])

}
