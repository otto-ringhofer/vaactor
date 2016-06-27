package org.vaadin.addons.vaactor.demo

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactor.VaactorServlet

import akka.actor.Props

/** define servlet, url pattern and ui-class to start
  *
  * @author Otto Ringhofer
  */
@WebServlet(urlPatterns = Array("/*"))
class ChatServlet extends VaactorServlet(classOf[ChatUI]) {

  /** define session actor to be created for every session */
  override val sessionProps: Props = Props(classOf[ChatSession.SessionActor])

}
