package org.vaadin.addons.vaactor.demo

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactor.VaactorServlet
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
class SessionServlet extends VaactorServlet {

  /** Define session actor to be created for every session */
  override val sessionProps = Some(Props(classOf[Session.SessionActor]))

}
