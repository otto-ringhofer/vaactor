package org.vaadin.addons.vaactor

import java.io.{ BufferedReader, InputStream }
import java.security.Principal
import java.util
import java.util.Locale
import javax.servlet.http.Cookie

import VaactorUISpec._
import com.vaadin.server.{ VaadinRequest, VaadinService, WrappedSession }

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

  class TestUI extends VaactorUI {

    override def initVaactorUI(request: ScaladinRequest): Unit = ???

    def receive = ???

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

  it should "not create ui actor without call to init" in {
    val ui = new TestUI()
    ui.self shouldBe null
  }

  "VaactorUI.actorOf" should "create actor with proper name" in {
    val ui = new TestUI()
    val actor = VaactorUI.actorOf(Props(classOf[VaactorUIActor], ui))
    actor.path.name should startWith("ui-VaactorUIActor-")
  }


  it should "create actor calling receive" in {
    val ui = new TestUI()
    val actor = VaactorUI.actorOf(Props(classOf[VaactorUIActor], ui))
    actor ! "$test"
  }

}
