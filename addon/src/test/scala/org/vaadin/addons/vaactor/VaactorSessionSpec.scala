package org.vaadin.addons.vaactor

import VaactorSessionSpec._
import org.vaadin.addons.vaactor.VaactorSession._

import akka.actor._
import akka.testkit._

object VaactorSessionSpec {

  case class SessionState(state: String)

  case object Crash

  val InitialState = SessionState("Hello")
  val ChangedState = SessionState("Changed")

  class SessionActor extends Actor with VaactorSession[SessionState] {
    override val initialSessionState: SessionState = InitialState
    override val sessionBehaviour: Receive = {
      case ns: SessionState => sessionState = ns
      case Crash => throw new UnsupportedOperationException(Crash.toString)
    }
  }

  def kill(actor: ActorRef): Unit = actor ! PoisonPill


}

class VaactorSessionSpec extends AkkaSpec {

  "object VaactorSession" - {
    "guardian" - {
      "must not be null" in {
        VaactorSession.guardian should not be null
      }
      "must respond to Props Message with ActorRef with correct path" in {
        VaactorSession.guardian ! TestActors.echoActorProps
        val ref = expectMsgType[ActorRef](waittime)
        ref.path.toStringWithoutAddress should startWith("/user/session/session-EchoActor-")
        kill(ref)
      }

    }
    "actorOf" - {
      "must create actor with correct path supervised by guardian" in {
        val ref = VaactorSession.actorOf(TestActors.echoActorProps)
        ref.path.toStringWithoutAddress should startWith("/user/session/session-EchoActor-")
        kill(ref)
      }
    }
  }

  "trait VaactorSession - SessionActor extends VaactorSession" - {
    "Creation" - {
      "must be created with correct name by VaactorSession.actorOf" in {
        val sa: ActorRef = VaactorSession.actorOf(Props[SessionActor])
        sa.path.toStringWithoutAddress should startWith("/user/session/session-SessionActor-")
        kill(sa)
      }
    }
    "SessionState handling" - {
      "sessionState must write and read state" in {
        val sa = TestActorRef[SessionActor]
        sa.underlyingActor.sessionState shouldBe InitialState
        sa.underlyingActor.sessionState = ChangedState
        sa.underlyingActor.sessionState shouldBe ChangedState
        kill(sa)
      }
      "must initialize state and respond to RequestSessionState with initialState" in {
        val sa: ActorRef = VaactorSession.actorOf(Props[SessionActor])
        sa ! RequestSessionState
        expectMsgType[SessionState](waittime) shouldBe InitialState
        lastSender shouldBe self
        kill(sa)
      }
      "must restart after crash and respond to RequestSessionState with initialState" in {
        val sa: ActorRef = VaactorSession.actorOf(Props[SessionActor])
        sa ! Crash
        sa ! RequestSessionState
        expectMsgType[SessionState](waittime) shouldBe InitialState
        lastSender shouldBe self
        kill(sa)
      }
      "must set ChangedState and respond to RequestSessionState with ChangedState" in {
        val sa: ActorRef = VaactorSession.actorOf(Props[SessionActor])
        sa ! ChangedState
        sa ! RequestSessionState
        expectMsgType[SessionState](waittime) shouldBe ChangedState
        lastSender shouldBe self
        kill(sa)
      }
      "muss restart after crash and respond to RequestSessionState with ChangedState" in {
        val sa: ActorRef = VaactorSession.actorOf(Props[SessionActor])
        sa ! ChangedState
        sa ! Crash
        sa ! RequestSessionState
        expectMsgType[SessionState](waittime) shouldBe ChangedState
        lastSender shouldBe self
        kill(sa)
      }
    }
    "Subscriber handling" - {
      "must manage sender in subscribers on Subscribe and Unsubscribe" in {
        val sa = TestActorRef[SessionActor]
        val tp = TestProbe()
        val subscribers = sa.underlyingActor.subscribers
        subscribers.size shouldBe 0 // initially empty
        sa ! Subscribe
        subscribers.size shouldBe 1 // sender added
        subscribers.contains(self) shouldBe true
        sa ! Subscribe
        subscribers.size shouldBe 1 // no duplicates
        subscribers.contains(self) shouldBe true
        sa ! Subscribe(tp.ref)
        subscribers.size shouldBe 2 // testprobe added
        subscribers.contains(self) shouldBe true
        subscribers.contains(tp.ref) shouldBe true
        sa ! Unsubscribe
        subscribers.size shouldBe 1 // sender removed
        subscribers.contains(self) shouldBe false
        subscribers.contains(tp.ref) shouldBe true
        sa ! Unsubscribe(tp.ref)
        subscribers.size shouldBe 0 // sender removed
        kill(sa)
      }
      "must send current session state to all subscribers on BroadcastSessionState" in {
        val sa: ActorRef = VaactorSession.actorOf(Props[SessionActor])
        val tp = TestProbe()
        sa ! Subscribe
        sa ! BroadcastSessionState
        expectMsgType[SessionState](waittime) shouldBe InitialState
        lastSender shouldBe self
        sa ! Subscribe(tp.ref)
        sa ! ChangedState
        sa ! BroadcastSessionState
        expectMsgType[SessionState](waittime) shouldBe ChangedState
        lastSender shouldBe self
        tp.expectMsgType[SessionState](waittime) shouldBe ChangedState
        tp.lastSender shouldBe self
        kill(sa)
      }
      "must send message to all subscribers on Broadcast(msg)" in {
        val sa: ActorRef = VaactorSession.actorOf(Props[SessionActor])
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
    "WithSession handling" - {
      "ForwardWithSession must forward WithSession to desired receiver" in {
        val sa: ActorRef = VaactorSession.actorOf(Props[SessionActor])
        val tp = TestProbe()
        val msg = "Hi"
        sa ! ForwardWithSession(msg, tp.ref)
        val a: WithSession[SessionState, String] = tp.expectMsgType[WithSession[SessionState, String]](waittime)
        a.session shouldBe InitialState
        a.msg shouldBe msg
        tp.lastSender shouldBe self
        kill(sa)
      }
      "BroadcastWithSession must forward WithSession to all subscribers" in {
        val sa: ActorRef = VaactorSession.actorOf(Props[SessionActor])
        val tp = TestProbe()
        val msg = "Hi"
        sa ! Subscribe
        sa ! Subscribe(tp.ref)
        sa ! BroadcastWithSession(msg)
        expectMsgType[WithSession[SessionState, String]](waittime) shouldBe WithSession(InitialState, msg)
        lastSender shouldBe self
        tp.expectMsgType[WithSession[SessionState, String]](waittime) shouldBe WithSession(InitialState, msg)
        tp.lastSender shouldBe self
        kill(sa)
      }
    }
  }

}
