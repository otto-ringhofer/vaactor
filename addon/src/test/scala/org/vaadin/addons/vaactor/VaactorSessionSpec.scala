package org.vaadin.addons.vaactor

import VaactorSessionSpec._
import org.vaadin.addons.vaactor.VaactorSession.{ BroadcastSession, RequestSession, SubscribeUI, UnsubscribeUI }

import akka.actor.Actor
import akka.testkit.TestActorRef

object VaactorSessionSpec {

  case class TestSession(state: String)

  val defaultSessionState = "$test$Session$"
  val DefaultSession = TestSession(defaultSessionState)
  val EmptySession = TestSession("")

  class SessionBehaviourActor extends Actor with VaactorSession[TestSession] {
    val session = DefaultSession

    val receive = sessionBehaviour
  }

}

class VaactorSessionSpec extends AkkaSpec {

  "VaactorSession.sessionBehaviour" should "reply session to sender on RequestSession" in {
    val actor = TestActorRef[SessionBehaviourActor]
    actor ! RequestSession
    expectMsgType[TestSession](waittime) shouldBe DefaultSession
  }

  it should "manage sender in uiActors on SubscribeUI and UnsubscribeUI" in {
    val actor = TestActorRef[SessionBehaviourActor]
    actor.underlyingActor.uiActors.size shouldBe 0 // initially empty
    actor ! SubscribeUI
    actor.underlyingActor.uiActors.size shouldBe 1 // sender added
    actor ! SubscribeUI
    actor.underlyingActor.uiActors.size shouldBe 1 // no duplicates
    actor ! UnsubscribeUI
    actor.underlyingActor.uiActors.size shouldBe 0 // sender removed
  }

  it should "broadcast session on BroadcastSession" in {
    val actor = TestActorRef[SessionBehaviourActor]
    actor ! SubscribeUI
    actor ! BroadcastSession
    expectMsgType[TestSession](waittime) shouldBe DefaultSession
  }

  "VaactorSession.broadcast" should "broadcast message to all registered Uis" in {
    val actor = TestActorRef[SessionBehaviourActor]
    actor ! SubscribeUI
    actor.underlyingActor.broadcast(defaultSessionState)
    expectMsgType[String](waittime) shouldBe defaultSessionState
  }

}
