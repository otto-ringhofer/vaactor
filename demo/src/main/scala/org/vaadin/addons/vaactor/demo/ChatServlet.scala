package org.vaadin.addons.vaactor.demo

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactor.VaactorServlet

import akka.actor.Props

@WebServlet(urlPatterns = Array("/*"))
class ChatServlet extends VaactorServlet(classOf[ChatUI]) {

  override val sessionProps: Props = Props(classOf[ChatSession.SessionActor])

}
