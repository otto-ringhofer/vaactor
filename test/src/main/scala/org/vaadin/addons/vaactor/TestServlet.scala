package org.vaadin.addons.vaactor

import javax.servlet.annotation.WebServlet

import Forwarder._
import TestServlet._
import com.vaadin.flow.server.VaadinServletConfiguration

import akka.actor.{ Actor, ActorRef, Props }

object TestServlet {

  case class SessionState(value: String)

  case object Attach

  case object Detach

  val EmptySessionState = SessionState("")

  class SessionActor extends Actor with VaactorSession[SessionState] {

    override val initialSessionState: SessionState = EmptySessionState

    override val sessionBehaviour: Receive = {
      case sst: SessionState => sessionState = sst
      case Attach => forwarder forward Forward(TestActorName, Attach)
      case Detach => forwarder forward Forward(TestActorName, Detach)
    }

    forwarder ! Register(SessionActorName)
  }

  val forwarder: ActorRef = Forwarder.forwarder

  // Dummy function, references object without side effects
  private def dummyInit(): Unit = {}

}

@WebServlet(
  urlPatterns = Array("/*"),
  asyncSupported = true
)
@VaadinServletConfiguration(
  productionMode = false
)
class TestServlet extends VaactorSessionServlet {

  override val sessionProps: Props = Props[SessionActor]

  dummyInit() // activate companion object and Forwarder referenced there

}
