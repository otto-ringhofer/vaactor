package org.vaadin.addons.vaactor.example

import javax.servlet.annotation.WebServlet

import ExampleObject._
import org.vaadin.addons.vaactor.{ VaactorServlet, VaactorSession, VaactorUI }
import com.vaadin.annotations.{ Push, VaadinServletConfiguration }
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.ui.Transport
import com.vaadin.ui._
import com.vaadin.ui.themes.ValoTheme

import akka.actor.{ Actor, Props }

/**
  * @author Otto Ringhofer
  */

object ExampleObject {
  var globalCnt = 0
}

@WebServlet(
  urlPatterns = Array("/*"),
  asyncSupported = true)
@VaadinServletConfiguration(
  productionMode = false,
  ui = classOf[ExampleUI])
class ExampleServlet extends VaactorServlet {

  override val sessionProps: Option[Props] = Some(Props(classOf[ExampleSessionActor]))

}

@Push(value = PushMode.AUTOMATIC, transport = Transport.WEBSOCKET)
class ExampleUI extends VaactorUI {

  var uiCnt = 0

  val layout = new VerticalLayout {
    setMargin(true)
    setSpacing(true)
    addComponent(new Label {
      setValue("Vaactor Example")
      addStyleName(ValoTheme.LABEL_H1)
    })
    addComponent(new Button("Click Me", { _ =>
      uiCnt += 1
      vaactorUI.send2SessionActor(s"Thanks for clicking! (uiCnt:$uiCnt)")
    })
    )
  }

  override def init(request: VaadinRequest): Unit = { setContent(layout) }

  def receive: PartialFunction[Any, Unit] = {
    case hello: String =>
      globalCnt += 1
      layout.addComponent(new Label(s"$hello (globalCnt:$globalCnt)"))
  }

}

class ExampleSessionActor extends Actor with VaactorSession[Int] {

  override val initialSessionState = 0

  override val sessionBehaviour: Receive = {
    case msg: String =>
      sessionState += 1
      sender ! s"$msg (sessionCnt:$sessionState)"
  }

}
