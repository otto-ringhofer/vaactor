package org.vaadin.addons.vaactor

import VaactorSpec._
import VaactorUISpec._

import akka.actor.Actor.Receive
import akka.actor.ActorRef

object VaactorSpec {

  case class VaactorTestMsg(msg: String, probe: ActorRef)

  class TestVaactor(val vaactorUI: VaactorUI) extends Vaactor {

    def receive: Receive = {
      case VaactorTestMsg(msg, probe) => probe ! msg
    }

  }

}

class VaactorSpec extends AkkaSpec {

  "Vaactor should" - {
    "create self actor" in {
      val ui = new TestUI()
      val va = new TestVaactor(ui)
      va.self.path.name should startWith("ui-UiActor-")
      va.self.path.name should include("-VaactorProxyActor-")
    }
  }

}


