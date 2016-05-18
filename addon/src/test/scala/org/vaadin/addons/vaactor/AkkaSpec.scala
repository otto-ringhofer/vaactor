package org.vaadin.addons.vaactor

import org.scalatest.{ BeforeAndAfterAll, FlatSpecLike, Inside, Matchers }

import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit }

import scala.concurrent.duration._

abstract class AkkaSpec extends TestKit(VaactorServlet.system)
  with FlatSpecLike with BeforeAndAfterAll with Matchers with Inside
  with DefaultTimeout with ImplicitSender {

  implicit val waittime = 500.millis

  override def afterAll {
    shutdown()
  }

}
