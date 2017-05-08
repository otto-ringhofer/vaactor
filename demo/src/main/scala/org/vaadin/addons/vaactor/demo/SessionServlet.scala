package org.vaadin.addons.vaactor.demo

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactor.VaactorServlet
import com.vaadin.annotations.VaadinServletConfiguration

import akka.actor.Props

/** Define servlet, url pattern and ui-class to start
  *
  * @author Otto Ringhofer
  */
@WebServlet(
  urlPatterns = Array("/session/*", "/VAADIN/*"),
  asyncSupported = true)
@VaadinServletConfiguration(
  productionMode = false,
  ui = classOf[SessionUI])
class SessionServlet extends VaactorServlet {

  /** Define session actor to be created for every session */
  override val sessionProps = Some(Props(classOf[Session.SessionActor]))

}
