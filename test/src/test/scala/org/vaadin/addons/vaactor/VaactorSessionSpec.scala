package org.vaadin.addons.vaactor

import Forwarder._
import TestServlet._

import akka.actor.{ ActorIdentity, ActorRef, Identify }

class VaactorSessionSpec extends WebBrowserSpec {

  var forwarder: ActorRef = _

  "remote ActorSystem should be found" in {
    VaactorServlet.system.actorSelection(ForwarderPath) ! Identify("")
    val id = expectMsgType[ActorIdentity]
    id.ref should not be None
    id.ref.get.path.toString shouldBe ForwarderPath
    forwarder = id.ref.get // store for later use
  }

  "remote Session Actor should" - {
    "be created" in {
      forwarder ! Lookup(SessionActorName)
      val reg = expectMsgType[Registered]
      reg.name shouldBe SessionActorName
      reg.actor.path.toString should startWith(RemoteSystemPath + "/user/session/session-SessionActor-")
    }
    "respond to RequestSessionState" in {
      forwarder ! Lookup(SessionActorName)
      val reg = expectMsgType[Registered]
      reg.actor ! VaactorSession.RequestSessionState
      val sst = expectMsgType[SessionState]
      sst shouldBe EmptySessionState
    }
    "change SessionState" in {
      val state = "Hello Daisy!"
      forwarder ! Lookup(SessionActorName)
      val reg = expectMsgType[Registered]
      reg.actor ! SessionState(state)
      reg.actor ! VaactorSession.RequestSessionState
      val sst = expectMsgType[SessionState]
      sst.value shouldBe state
    }
  }


}
