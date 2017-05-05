package org.vaadin.addons.vaactor

import akka.actor.ActorRef
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.scalatest.selenium.WebBrowser
import org.scalatest.time.{ Seconds, Span }
import Forwarder.{ Register, TestActorName }

abstract class WebBrowserSpec extends AkkaSpec with WebBrowser {

  // install ChromeDriver from https://sites.google.com/a/chromium.org/chromedriver/
  // include the ChromeDriver location in your PATH environment variable

  val Host = "http://localhost:8080"

  implicit val webDriver: WebDriver = new ChromeDriver()
  implicitlyWait(Span(5, Seconds))
  go to Host // Remote Service initialisieren

  val LocalSysstemPath = "akka://test-client"
  val RemoteSystemPath = "akka.tcp://test-service@127.0.0.1:2552"
  val ForwarderPath: String = RemoteSystemPath + "/user/tools/forwarder"
// todo  val forwarder: ActorRef = VaactorServlet.lookupActor(ForwarderPath).get
// todo  forwarder ! Register(TestActorName)

  override def afterAll {
    super.afterAll
    quit()
  }

}
