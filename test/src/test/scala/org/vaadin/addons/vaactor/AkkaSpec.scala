package org.vaadin.addons.vaactor

import org.scalatest.{ BeforeAndAfterAll, FreeSpecLike, Inside, Matchers }

import akka.testkit.{ DefaultTimeout, ImplicitSender, TestKit }

abstract class AkkaSpec extends TestKit(VaactorServlet.system)
  with FreeSpecLike with BeforeAndAfterAll with Matchers with Inside with DefaultTimeout with ImplicitSender {

  override def afterAll = {
    super.afterAll
    VaactorServlet.system.terminate()
  }

}
