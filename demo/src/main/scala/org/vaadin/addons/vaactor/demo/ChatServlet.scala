package org.vaadin.addons.vaactor.demo

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactor.VaactorServlet
import com.vaadin.annotations.VaadinServletConfiguration

import akka.actor.Props

/** define servlet, url pattern and ui-class to start
  *
  * @author Otto Ringhofer
  */
@WebServlet(
  urlPatterns = Array("/*"),
  asyncSupported = true)
@VaadinServletConfiguration(
  productionMode = false,
  ui = classOf[ChatUI])
class ChatServlet extends VaactorServlet {

  /** define session actor to be created for every session */
  override val sessionProps: Props = Props(classOf[ChatSession.SessionActor])

}
