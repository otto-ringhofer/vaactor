package org.vaadin.addons.vaactor

import VaactorSpec._
import VaactorUISpec._

import akka.actor.ActorRef

object VaactorSpec {

  case class VaactorTestMsg(msg: String, probe: ActorRef)

  class TestVaactor(val vaactorUI: VaactorUI) extends Vaactor {

    def receive = {
      case VaactorTestMsg(msg, probe) => probe ! msg
    }

  }

}

class VaactorSpec extends AkkaSpec {

  "Vaactor" should "create self actor" in {
    val ui = new TestUI()
    val va = new TestVaactor(ui)
    va.self.path.name shouldBe "vaactor-UiGuardian-1-VaactorActor-1"
  }

  it should "create actor calling receive" in {
    val ui = new TestUI()
    val va = new TestVaactor(ui)
    va.self ! VaactorTestMsg("$test", self)
    expectMsg("$test")
  }

}
