package org.vaadin.addons.vaactor

import Forwarder._
import TestServlet._
import TestUI._

import akka.actor.{ ActorIdentity, ActorRef, Identify }

class VaactorUISpec extends WebBrowserSpec {

  var forwarder: ActorRef = _

  "remote ActorSystem should be found" in {
    VaactorServlet.system.actorSelection(ForwarderPath) ! Identify("")
    val id = expectMsgType[ActorIdentity]
    id.ref should not be None
    id.ref.get.path.toString shouldBe ForwarderPath
    forwarder = id.ref.get // store for later use
  }

  "remote UI Actor should" - {
    "be created" in {
      forwarder ! Lookup(UIActorName)
      val reg = expectMsgType[Registered]
      reg.name shouldBe UIActorName
      reg.actor.path.toString should startWith(RemoteSystemPath + "/user/ui/ui-UiActor-")
    }
  }

  "remote UI should" - {
    "return text in 'text' field " in {
      val txt = textField(TextName)
      txt.value shouldBe "akka.actor.provider remote vaactor.system-name test-server"
    }
    "set SessionState on ButtonClick" in {
      val testState = "$Hurzi"
      forwarder ! Lookup(SessionActorName)
      val reg = expectMsgType[Registered]
      reg.actor ! VaactorSession.RequestSessionState
      expectMsgType[SessionState] shouldBe EmptySessionState
      textField(TextName).value = testState
      click on ButtonName
      Thread.sleep(100) // maybe Click needs some time
      reg.actor ! VaactorSession.RequestSessionState
      expectMsgType[SessionState] shouldBe SessionState(testState)
    }
  }

}
