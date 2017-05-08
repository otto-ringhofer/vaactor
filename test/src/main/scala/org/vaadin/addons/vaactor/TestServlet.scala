package org.vaadin.addons.vaactor

import javax.servlet.annotation.WebServlet

import Forwarder._
import com.vaadin.annotations.VaadinServletConfiguration

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
  def touchTestServlet(): Unit = {}

}

@WebServlet(
  urlPatterns = Array("/*"),
  asyncSupported = true
)
@VaadinServletConfiguration(
  productionMode = false,
  ui = classOf[TestUI]
)
class TestServlet extends VaactorServlet {

  import TestServlet._

  override val sessionProps = Some(Props[SessionActor])

  touchTestServlet() // activate companion object and Forwarder referenced there

}
