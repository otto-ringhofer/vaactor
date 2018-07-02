package org.vaadin.addons.vaactor

import TraitNestingSpec._
import org.scalatest.FreeSpec

import akka.actor.Actor.Receive
import akka.actor.ActorRef

object TraitNestingSpec {

  case class VaactorTestMsg(msg: String, probe: ActorRef)

  class TestVaactor extends Vaactor.HasActor {

    def receive: Receive = {
      case VaactorTestMsg(msg, probe) => probe ! msg
    }

  }

  class Comp {
    def onAttach(): Unit = println("Comp.onAttach")
  }

  trait HasAct extends Comp {
    override def onAttach(): Unit = {
      super.onAttach()
      println("HasAct.onAttach")
    }
  }

  trait HasSes extends Comp {
    var ses: String = _

    override def onAttach(): Unit = {
      super.onAttach()
      ses = "HasSes"
      println(s"$ses.onAttach")
    }

    def sess(): Unit = println(s"$ses.sess")
  }

  trait SubSesWrong extends Comp {
    this: HasSes =>
    override def onAttach(): Unit = {
      super.onAttach()
      sess()
      println("SubSes.onAttach")
    }
  }

  trait SubSes extends HasSes {
    override def onAttach(): Unit = {
      super.onAttach()
      sess()
      println("SubSes.onAttach")
    }
  }

  class A extends Comp with HasAct

  class S extends Comp with HasSes

  class AS extends Comp with HasAct with HasSes

  class SA extends Comp with HasSes with HasAct

  class SU extends Comp with HasSes with SubSes

  class AU extends Comp with HasAct with SubSes

  class ASU extends Comp with HasAct with HasSes with SubSes

  class SAU extends Comp with HasSes with HasAct with SubSes

  class AWS extends Comp with HasAct with SubSesWrong with HasSes // todo - dangerous!!

  class AUS extends Comp with HasAct with SubSes with HasSes

  class SUA extends Comp with HasSes with SubSes with HasAct

}

class TraitNestingSpec extends FreeSpec {

  "Test Trait Patterns" - {
    "A" in {
      println("A =============>")
      new A().onAttach()
    }
    "S" in {
      println("SA =============>")
      new S().onAttach()
    }
    "AS" in {
      println("AS =============>")
      new AS().onAttach()
    }
    "SA" in {
      println("SA =============>")
      new SA().onAttach()
    }
    "SU" in {
      println("SU =============>")
      new SU().onAttach()
    }
    "AU" in {
      println("AU =============>")
      new AU().onAttach()
    }
    "ASU" in {
      println("ASU =============>")
      new ASU().onAttach()
    }
    "SAU" in {
      println("SAU =============>")
      new SAU().onAttach()
    }
    "AWS" in {
      println("AWS =============> (dangerous!)")
      new AWS().onAttach() // todo dangerous!!
    }
    "AUS" in {
      println("AUS =============>")
      new AUS().onAttach()
    }
    "SUA" in {
      println("SUA =============>")
      new SUA().onAttach()
    }
  }

}


