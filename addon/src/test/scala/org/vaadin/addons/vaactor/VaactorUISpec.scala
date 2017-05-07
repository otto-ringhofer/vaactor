package org.vaadin.addons.vaactor

import VaactorUISpec._
import org.vaadin.addons.vaactor.VaactorSession._
import org.vaadin.addons.vaactor.VaactorSessionSpec.kill
import org.vaadin.addons.vaactor.VaactorUI._
import com.vaadin.server.VaadinRequest

import akka.actor.{ ActorRef, Props }
import akka.testkit.{ TestActorRef, TestActors, TestProbe }

object VaactorUISpec {

  case class UiTestMsg(msg: String, probe: ActorRef)

  class TestUI extends VaactorUI {

    override def init(request: VaadinRequest): Unit = {}

  }

}

class VaactorUISpec extends AkkaSpec {
  "object VaactorUI" - {
    "guardian" - {
      "must not be null" in {
        VaactorUI.guardian should not be null
      }
      "must respond to Props Message with ActorRef with correct path" in {
        VaactorUI.guardian ! TestActors.echoActorProps
        val ref = expectMsgType[ActorRef](waittime)
        ref.path.toStringWithoutAddress should startWith("/user/ui/ui-EchoActor-")
        kill(ref)
      }
    }
    "actorOf" - {
      "must create actor with correct path supervised by guardian" in {
        val ref = VaactorUI.actorOf(TestActors.echoActorProps)
        ref.path.toStringWithoutAddress should startWith("/user/ui/ui-EchoActor-")
        kill(ref)
      }
    }
  }

  "class VaactorUI.UiActor" - {
    "must manage sender in subscribers on Subscribe and Unsubscribe" in {
      val uia = TestActorRef[VaactorUI.UiActor]
      val tp = TestProbe()
      val subscribers = uia.underlyingActor.subscribers
      subscribers.size shouldBe 0 // initially empty
      uia ! Subscribe
      subscribers.size shouldBe 1 // sender added
      subscribers.contains(self) shouldBe true
      uia ! Subscribe
      subscribers.size shouldBe 1 // no duplicates
      subscribers.contains(self) shouldBe true
      uia ! Subscribe(tp.ref)
      subscribers.size shouldBe 2 // testprobe added
      subscribers.contains(self) shouldBe true
      subscribers.contains(tp.ref) shouldBe true
      uia ! Unsubscribe
      subscribers.size shouldBe 1 // sender removed
      subscribers.contains(self) shouldBe false
      subscribers.contains(tp.ref) shouldBe true
      uia ! Unsubscribe(tp.ref)
      subscribers.size shouldBe 0 // sender removed
      kill(uia)
    }
    "must send message to all subscribers on Broadcast(msg)" in {
      val sa: ActorRef = VaactorUI.actorOf(Props[UiActor])
      val tp = TestProbe()
      val msg = "Hi"
      sa ! Subscribe
      sa ! Subscribe(tp.ref)
      sa ! Broadcast(msg)
      expectMsgType[String](waittime) shouldBe msg
      lastSender shouldBe self
      tp.expectMsgType[String](waittime) shouldBe msg
      tp.lastSender shouldBe self
      kill(sa)
    }
  }

  "class VaactorUI - TestUI extends VaactorUI" - {
    "uiActor" - {
      val ui = new TestUI()
      "must be created with correct name" in {
        ui.uiActor.path.toStringWithoutAddress should startWith("/user/ui/ui-UiActor-")
      }
      "must send message to all subscribers on Broadcast(msg)" in {
        val ui = new TestUI()
        val tp = TestProbe()
        val msg = "Hi"
        ui.uiActor ! Subscribe
        ui.uiActor ! Subscribe(tp.ref)
        ui.uiActor ! Broadcast(msg)
        expectMsgType[String](waittime) shouldBe msg
        lastSender shouldBe self
        tp.expectMsgType[String](waittime) shouldBe msg
        tp.lastSender shouldBe self
      }
      kill(ui.uiActor)
    }
  }

}
