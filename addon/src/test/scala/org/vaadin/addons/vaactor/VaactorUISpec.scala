package org.vaadin.addons.vaactor


import VaactorUISpec._
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.PushMode

import akka.actor.{ ActorRef, Props }

object VaactorUISpec {

  class ParamUI(title: String, theme: String, widgetset: String,
    preserveOnRefresh: Boolean, pushMode: PushMode)
    extends VaactorUI {

    override def init(request: VaadinRequest): Unit = ???

    def receive = ???

  }

  case class UiTestMsg(msg: String, probe: ActorRef)

  class TestUI extends VaactorUI {

    override def init(request: VaadinRequest): Unit = ???

    def receive: PartialFunction[Any, Unit] = {
      case UiTestMsg(msg, probe) => probe ! msg
    }

    override def access(runnable: Runnable) = ???

  }

}

class VaactorUISpec extends AkkaSpec {
/*
  "VaactorUI" should "set default constructor parameters" in {
    val ui = new TestUI()
    ui.getCaption shouldBe None
    ui.getTheme shouldBe null
  }

  it should "set specific constructor parameters" in {
    val ui = new ParamUI(title = "$title", theme = "$theme", widgetset = "$widgetset",
      preserveOnRefresh = true, pushMode = PushMode.MANUAL)
    ui.getCaption shouldBe Some("$title")
    ui.getTheme shouldBe "$theme"
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
*/
}
