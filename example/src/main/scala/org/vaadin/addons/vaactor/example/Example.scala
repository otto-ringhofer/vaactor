package org.vaadin.addons.vaactor.example

import javax.servlet.annotation.WebServlet

import ExampleObject.globalCnt
import org.vaadin.addons.vaactor._
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
  // global counter
  var globalCnt = 0
}

@WebServlet(
  urlPatterns = Array("/*"),
  asyncSupported = true
)
@VaadinServletConfiguration(
  productionMode = false,
  ui = classOf[ExampleUI]
)
class ExampleServlet extends VaactorServlet {

  override val sessionProps: Option[Props] = Some(Props(classOf[ExampleSessionActor]))

}

@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
class ExampleUI extends VaactorUI with Vaactor.UIVaactor {

  // counter local to this UI
  var uiCnt = 0

  val stateDisplay = new Label()
  val layout: VerticalLayout = new VerticalLayout {
    setMargin(true)
    setSpacing(true)
    addComponent(new Label("Vaactor Example") {
      addStyleName(ValoTheme.LABEL_H1)
    })
    addComponent(new Button("Click Me", { _ =>
      uiCnt += 1
      send2SessionActor(s"Thanks for clicking! (uiCnt:$uiCnt)")
    })
    )
    addComponent(stateDisplay)
  }

  override def init(request: VaadinRequest): Unit = { setContent(layout) }

  override def receive: Actor.Receive = {
    case hello: String =>
      globalCnt += 1
      stateDisplay.setValue(s"$hello (globalCnt:$globalCnt)")
  }

}

class ExampleSessionActor extends Actor with VaactorSession[Int] {
  // state is session counter
  override val initialSessionState = 0

  override val sessionBehaviour: Receive = {
    case msg: String =>
      sessionState += 1
      sender ! s"$msg (sessionCnt:$sessionState)"
  }

}
