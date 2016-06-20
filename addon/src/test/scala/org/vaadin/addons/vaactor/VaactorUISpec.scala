package org.vaadin.addons.vaactor

import VaactorUISpec._

import akka.actor.Props
import vaadin.scala.PushMode
import vaadin.scala.server.ScaladinRequest

object VaactorUISpec {

  class ParamUI(title: String, theme: String, widgetset: String,
    preserveOnRefresh: Boolean, pushMode: PushMode.Value)
    extends VaactorUI(title, theme, widgetset, preserveOnRefresh, pushMode) {

    override def initVaactorUI(request: ScaladinRequest): Unit = ???

    def receive = ???

  }

  class TestUI extends VaactorUI {

    override def initVaactorUI(request: ScaladinRequest): Unit = ???

    def receive = {
      case a: Any => println(s"TestUI received $a")
    }

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

  "VaactorUI.actorOf" should "create actor with proper name" in {
    val ui = new TestUI()
    val actor = VaactorUI.actorOf(Props(classOf[VaactorActor], ui))
    actor.path.name should startWith("ui-VaactorActor-")
  }


  it should "create actor calling receive" in {
    val ui = new TestUI()
    val actor = VaactorUI.actorOf(Props(classOf[VaactorActor], ui))
    actor ! "$test" // generates UIDetachedException :-(
  }

}
