package org.vaadin.addons.vaactor

import VaactorSessionSpec._
import org.vaadin.addons.vaactor.VaactorSession._

import akka.actor.{ Actor, Props }
import akka.testkit.TestActorRef

object VaactorSessionSpec {

  case class TestSessionState(state: String)

  case object Crash

  val defaultSessionState = "$test$Session$"
  val DefaultSessionState = TestSessionState(defaultSessionState)
  val EmptySessionState = TestSessionState("")

  class TestActor extends Actor with VaactorSession[TestSessionState] {

    val initialSessionState = DefaultSessionState

    override val sessionBehaviour: Receive = {
      case s: TestSessionState => sessionState = s
      case Crash => throw new Exception("Crash received")
    }

  }

  val testProps: Props = Props[TestActor]
}

class VaactorSessionSpec extends AkkaSpec {

  "VaactorSession.sessionState" should "set and return session state" in {
    val actor = TestActorRef[TestActor]
    actor.underlyingActor.sessionState shouldBe DefaultSessionState
    actor.underlyingActor.sessionState = EmptySessionState
    actor.underlyingActor.sessionState shouldBe EmptySessionState
  }

  "VaactorSession.sessionBehaviour" should "reply session state to sender on RequestSessionState" in {
    val actor = TestActorRef[TestActor]
    actor ! RequestSessionState
    expectMsgType[TestSessionState](waittime) shouldBe DefaultSessionState
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

  it should "broadcast session state on BroadcastSessionState" in {
    val actor = TestActorRef[TestActor]
    actor ! SubscribeUI
    actor ! BroadcastSessionState
    expectMsgType[TestSessionState](waittime) shouldBe DefaultSessionState
  }

  it should "set session state on receive of SessionState" in {
    val actor = TestActorRef[TestActor]
    actor ! RequestSessionState
    expectMsgType[TestSessionState](waittime) shouldBe DefaultSessionState
    actor ! EmptySessionState
    actor ! RequestSessionState
    expectMsgType[TestSessionState](waittime) shouldBe EmptySessionState
  }

  "VaactorSession.broadcast" should "broadcast message to all registered Uis" in {
    val actor = TestActorRef[TestActor]
    actor ! SubscribeUI
    actor.underlyingActor.broadcast(defaultSessionState)
    expectMsgType[String](waittime) shouldBe defaultSessionState
  }

  "VaadinSession.actorOf" should "return restarting ActorRef" in {
    val actor = VaactorSession.actorOf(testProps)
    actor ! RequestSessionState
    expectMsgType[TestSessionState](waittime) shouldBe DefaultSessionState
    actor ! Crash
    actor ! RequestSessionState
    expectMsgType[TestSessionState](waittime) shouldBe DefaultSessionState
  }

  it should "keep session state when restarted" in {
    val actor = VaactorSession.actorOf(testProps)
    actor ! RequestSessionState
    expectMsgType[TestSessionState](waittime) shouldBe DefaultSessionState
    actor ! EmptySessionState
    actor ! RequestSessionState
    expectMsgType[TestSessionState](waittime) shouldBe EmptySessionState
    actor ! Crash
    actor ! RequestSessionState
    expectMsgType[TestSessionState](waittime) shouldBe EmptySessionState
  }

}
