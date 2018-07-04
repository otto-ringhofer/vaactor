package org.vaadin.addons.vaactor

import Forwarder._

import akka.actor.{ ActorIdentity, ActorRef, Identify }

class VaactorSessionServletSpec extends WebBrowserSpec {

  var forwarder: ActorRef = _

  "local ActorSystem should be configured" - {
    "should have correct config" in {
      config.getString("system-name") shouldBe "test-client"
      VaactorServlet.system.name shouldBe "test-client"
    }
  }

  "remote ActorSystem should be configured" - {
    "should access remote forwarder actor" in {
      VaactorServlet.system.actorSelection(ForwarderPath) ! Identify("")
      val id = expectMsgType[ActorIdentity]
      id.ref should not be None
      id.ref.get.path.toString shouldBe ForwarderPath
      forwarder = id.ref.get // store for later use
    }
  }

  "remote Forwarder should" - {
    "deliver local testProbe" in {
      val myself = "myself"
      forwarder ! Register(myself)
      forwarder ! Lookup(myself)
      val reg = expectMsgType[Registered]
      reg.name shouldBe myself
      reg.actor shouldBe self
    }
    "forward to local testProbe" in {
      forwarder ! Register(TestActorName)
      forwarder ! Forward(TestActorName, "Hello")
      expectMsgType[String] shouldBe "Hello"
      forwarder ! Forward(TestActorName, 18)
      expectMsgType[Int] shouldBe 18
    }
  }

  "TestServlet called without path" - {
    "should return version as title" in {
      pageTitle should be("")
      pageSource should startWith("<!DOCTYPE html><html xmlns=\"http://www.w3.org/1999/xhtml\">")
    }
  }

}
