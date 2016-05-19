package org.vaadin.addons.vaactor

import VaactorSessionSpec._
import org.vaadin.addons.vaactor.VaactorSession._

import akka.actor.{ Actor, Props }
import akka.testkit.TestActorRef

object VaactorSessionSpec {

  case class TestSession(state: String)

  case object Crash

  val defaultSessionState = "$test$Session$"
  val DefaultSession = TestSession(defaultSessionState)
  val EmptySession = TestSession("")

  class TestActor extends Actor with VaactorSession[TestSession] {
    def initialSession = DefaultSession

    val testBehaviour: Receive = {
      case s: TestSession => session = s
      case Crash => throw new Exception("Crash received")
    }
    val receive = sessionBehaviour orElse testBehaviour
  }

  val testProps = Props[TestActor]
}

class VaactorSessionSpec extends AkkaSpec {

  "VaactorSession.session" should "set and return session" in {
    val actor = TestActorRef[TestActor]
    actor.underlyingActor.session shouldBe DefaultSession
    actor.underlyingActor.session = EmptySession
    actor.underlyingActor.session shouldBe EmptySession
  }

  "VaactorSession.sessionBehaviour" should "reply session to sender on RequestSession" in {
    val actor = TestActorRef[TestActor]
    actor ! RequestSession
    expectMsgType[TestSession](waittime) shouldBe DefaultSession
  }

  it should "manage sender in uiActors on SubscribeUI and UnsubscribeUI" in {
    val actor = TestActorRef[TestActor]
    actor.underlyingActor.uiActors.size shouldBe 0 // initially empty
    actor ! SubscribeUI
    actor.underlyingActor.uiActors.size shouldBe 1 // sender added
    actor ! SubscribeUI
    actor.underlyingActor.uiActors.size shouldBe 1 // no duplicates
    actor ! UnsubscribeUI
    actor.underlyingActor.uiActors.size shouldBe 0 // sender removed
  }

  it should "broadcast session on BroadcastSession" in {
    val actor = TestActorRef[TestActor]
    actor ! SubscribeUI
    actor ! BroadcastSession
    expectMsgType[TestSession](waittime) shouldBe DefaultSession
  }

  it should "set session on receive of Session" in {
    val actor = TestActorRef[TestActor]
    actor ! RequestSession
    expectMsgType[TestSession](waittime) shouldBe DefaultSession
    actor ! EmptySession
    actor ! RequestSession
    expectMsgType[TestSession](waittime) shouldBe EmptySession
  }

  "VaactorSession.broadcast" should "broadcast message to all registered Uis" in {
    val actor = TestActorRef[TestActor]
    actor ! SubscribeUI
    actor.underlyingActor.broadcast(defaultSessionState)
    expectMsgType[String](waittime) shouldBe defaultSessionState
  }

  "VaadinSession.actorOf" should "return restarting ActorRef" in {
    val actor = VaactorSession.actorOf(testProps)
    actor ! RequestSession
    expectMsgType[TestSession](waittime) shouldBe DefaultSession
    actor ! Crash
    actor ! RequestSession
    expectMsgType[TestSession](waittime) shouldBe DefaultSession
  }

  it should "kepp session when restarted" in {
    val actor = VaactorSession.actorOf(testProps)
    actor ! RequestSession
    expectMsgType[TestSession](waittime) shouldBe DefaultSession
    actor ! EmptySession
    actor ! RequestSession
    expectMsgType[TestSession](waittime) shouldBe EmptySession
    // actor ! Crash
    actor ! RequestSession
    expectMsgType[TestSession](waittime) shouldBe EmptySession
  }

}
