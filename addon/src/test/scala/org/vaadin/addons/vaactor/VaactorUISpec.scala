package org.vaadin.addons.vaactor


import VaactorUISpec._
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.PushMode

import akka.actor.{ ActorRef, Props }

object VaactorUISpec {

  class ParamUI(title: String, theme: String, widgetset: String,
    preserveOnRefresh: Boolean, pushMode: PushMode)
    extends VaactorUI(title, theme, widgetset, preserveOnRefresh, pushMode) {

    override def initVaactorUI(request: VaadinRequest): Unit = ???

    def receive = ???

  }

  case class UiTestMsg(msg: String, probe: ActorRef)

  class TestUI extends VaactorUI {

    override def initVaactorUI(request: VaadinRequest): Unit = ???

    def receive: PartialFunction[Any, Unit] = {
      case UiTestMsg(msg, probe) => probe ! msg
    }

    // TODO should return Future<Void> override def access(runnable: Runnable): Unit = runnable.run()

  }

}

class VaactorUISpec extends AkkaSpec {

  "VaactorUI" should "set default constructor parameters" in {
    val ui = new TestUI()
    ui.getCaption shouldBe None
    ui.getTheme shouldBe null
    // TODO ? ui.widgetset shouldBe None
    // TODO ? ui.preserveOnRefresh shouldBe false
    // TODO ? ui.pushMode shouldBe PushMode.Automatic
  }

  it should "set specific constructor parameters" in {
    val ui = new ParamUI(title = "$title", theme = "$theme", widgetset = "$widgetset",
      preserveOnRefresh = true, pushMode = PushMode.MANUAL)
    ui.getCaption shouldBe Some("$title")
    ui.getTheme shouldBe "$theme"
    // TODO ? ui.widgetset shouldBe Some("$widgetset")
    // TODO ? ui.preserveOnRefresh shouldBe true
    // TODO ? ui.pushMode shouldBe PushMode.Manual
  }

  it should "create uiGuardian" in {
    val ui = new TestUI()
    ui.uiGuardian.path.name shouldBe "vaactor-UiGuardian-1"
  }

  "VaactorUI.actorOf" should "create actor with proper name" in {
    val ui = new TestUI()
    val actor = ui.actorOf(Props(classOf[VaactorActor], ui))
    actor.path.name shouldBe "vaactor-UiGuardian-2-VaactorActor-1"
  }


  it should "create actor calling receive" in {
    val ui = new TestUI()
    val actor = ui.actorOf(Props(classOf[VaactorActor], ui))
    actor ! UiTestMsg("$test", self)
    expectMsg("$test")
  }

  it should "create self actor calling receive" in {
    val ui = new TestUI()
    ui.self ! UiTestMsg("$test", self)
    expectMsg("$test")
  }

}
