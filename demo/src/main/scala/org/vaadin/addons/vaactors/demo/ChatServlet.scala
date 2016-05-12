package org.vaadin.addons.vaactors.demo

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactors.VaactorsServlet

import akka.actor.Props

@WebServlet(urlPatterns = Array("/*"))
class ChatServlet extends VaactorsServlet(classOf[ChatUI]) {

  override val sessionProps: Props = Props(classOf[ChatSession.SessionActor])

}
