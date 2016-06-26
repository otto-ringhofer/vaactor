package org.vaadin.addons.vaactor

import VaactorUISpec._

import akka.actor.{ ActorRef, Props }
import vaadin.scala.PushMode
import vaadin.scala.server.ScaladinRequest

object VaactorUISpec {

  class ParamUI(title: String, theme: String, widgetset: String,
    preserveOnRefresh: Boolean, pushMode: PushMode.Value)
    extends VaactorUI(title, theme, widgetset, preserveOnRefresh, pushMode) {

    override def initVaactorUI(request: ScaladinRequest): Unit = ???

    def receive = ???

  }

  case class TestMsg(msg: String, probe: ActorRef)

  class TestUI extends VaactorUI {

    override def initVaactorUI(request: ScaladinRequest): Unit = ???

    def receive = {
      case TestMsg(msg, probe) => probe ! msg
    }

    override def access(runnable: => Unit): Unit = runnable

  }

}

class VaactorUISpec extends AkkaSpec {

  "VaactorUI" should "set default constructor parameters" in {
    val ui = new TestUI()
    ui.title shouldBe None
    ui.theme shouldBe null
    ui.widgetset shouldBe None
    ui.preserveOnRefresh shouldBe false
    ui.pushMode shouldBe PushMode.Automatic
  }

  it should "set specific constructor parameters" in {
    val ui = new ParamUI(title = "$title", theme = "$theme", widgetset = "$widgetset",
      preserveOnRefresh = true, pushMode = PushMode.Manual)
    ui.title shouldBe Some("$title")
    ui.theme shouldBe "$theme"
    ui.widgetset shouldBe Some("$widgetset")
    ui.preserveOnRefresh shouldBe true
    ui.pushMode shouldBe PushMode.Manual
  }

  it should "create uiGuardian" in {
    val ui = new TestUI()
    ui.uiGuardian.path.name should startWith("vaactor-UiGuardian-1")
  }

  "VaactorUI.actorOf" should "create actor with proper name" in {
    val ui = new TestUI()
    val actor = ui.actorOf(Props(classOf[VaactorActor], ui))
    actor.path.name should startWith("vaactor-UiGuardian-2-VaactorActor-1")
  }


  it should "create actor calling receive" in {
    val ui = new TestUI()
    val actor = Vaactor.actorOf(Props(classOf[VaactorActor], ui))
    actor ! TestMsg("$test", self)
    expectMsg("$test")
  }

}
