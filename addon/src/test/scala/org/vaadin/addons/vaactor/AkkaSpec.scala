package org.vaadin.addons.vaactor

import org.scalatest.{ BeforeAndAfterAll, FreeSpecLike, Inside, Matchers }

import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit }

import scala.concurrent.duration._

abstract class AkkaSpec extends TestKit(VaactorServlet.system)
  with FreeSpecLike with BeforeAndAfterAll with Matchers with Inside
  with DefaultTimeout with ImplicitSender {

  implicit val waittime: FiniteDuration = 500.millis

  override def afterAll {
  }

}
