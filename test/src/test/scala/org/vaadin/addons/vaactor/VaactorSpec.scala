package org.vaadin.addons.vaactor

import Forwarder._
import TestComponent._
import TestServlet._
import TestUI._

import akka.actor.{ ActorIdentity, ActorRef, Identify }

class VaactorSpec extends WebBrowserSpec {

  var forwarder: ActorRef = _

  "remote ActorSystem should be found" in {
    VaactorServlet.system.actorSelection(ForwarderPath) ! Identify("")
    val id = expectMsgType[ActorIdentity]
    id.ref should not be None
    id.ref.get.path.toString shouldBe ForwarderPath
    forwarder = id.ref.get // store for later use
    forwarder ! Register(TestActorName)
  }

  "remote Vaactor Actor should" - {
    "be created" in {
      forwarder ! Lookup(VaactorActorName)
      val reg = expectMsgType[Registered]
      reg.name shouldBe VaactorActorName
      reg.actor.path.toString should startWith(RemoteSystemPath + "/user/ui/ui-UiActor-")
      reg.actor.path.toString should include("-VaactorProxyActor-")
    }
  }

  "remote Vaactor should" - {
    "set SessionState on ButtonClick" in {
      val testState = "$Hurzi"
      forwarder ! Lookup(SessionActorName)
      val reg = expectMsgType[Registered]
      reg.actor ! VaactorSession.RequestSessionState
      expectMsgType[SessionState] shouldBe EmptySessionState
      textField(CompTextName).value = testState
      click on CompButtonName
      Thread.sleep(100) // maybe Click needs some time
      reg.actor ! VaactorSession.RequestSessionState
      expectMsgType[SessionState] shouldBe SessionState(testState)
    }
    "respond with Text-Content on RequestText(sender)" in {
      val testContent = "$Quaxi"
      forwarder ! Lookup(VaactorActorName)
      val reg = expectMsgType[Registered]
      textField(CompTextName).value = testContent
      click on CompButtonName // seems to trigger Vaadin transport
      Thread.sleep(100) // maybe Click needs some time
      reg.actor ! RequestText(self)
      expectMsgType[ReplyText] shouldBe ReplyText(testContent)
      lastSender shouldBe reg.actor
    }
    "respond with Text-Content on RequestText" in {
      val testContent = "$Murksi"
      forwarder ! Lookup(VaactorActorName)
      val reg = expectMsgType[Registered]
      textField(CompTextName).value = testContent
      click on CompButtonName // seems to trigger Vaadin transport
      Thread.sleep(100) // maybe Click needs some time
      reg.actor ! RequestText
      expectMsgType[ReplyText] shouldBe ReplyText(testContent)
      lastSender shouldBe reg.actor
    }
  }

  "remote VaactorComponentActor should" - {
    "be created and terminated" in {
      val testContent = "$Quaxi-dyn"
      click on AddComponentButtonName
      forwarder ! Lookup(VaactorActorName + NameSuffix)
      val reg = expectMsgType[Registered]
      textField(CompTextName + NameSuffix).value = testContent
      click on CompButtonName + NameSuffix // seems to trigger Vaadin transport
      reg.actor ! RequestText(self)
      expectMsgType[ReplyText] shouldBe ReplyText(testContent)
      lastSender shouldBe reg.actor
      click on RemoveComponentButtonName
      reg.actor ! RequestText(self)
      expectNoMsg()
    }

    "subscribe and unsubscribe" in {
      forwarder ! Lookup(SessionActorName)
      val reg = expectMsgType[Registered]
      reg.actor ! Attach
      expectMsgType[Attach.type]
      click on AddSubscriberButtonName
      expectMsgType[Attach.type]
      click on RemoveSubscriberButtonName
      expectMsgType[Detach.type]
    }
  }

}
