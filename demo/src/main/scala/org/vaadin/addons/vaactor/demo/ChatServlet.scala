package org.vaadin.addons.vaactor.demo

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactor.VaactorsServlet

import akka.actor.Props

@WebServlet(urlPatterns = Array("/*"))
class ChatServlet extends VaactorsServlet(classOf[ChatUI]) {

  override val sessionProps: Props = Props(classOf[ChatSession.SessionActor])

}
