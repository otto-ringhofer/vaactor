package org.vaadin.addons.vaactor.example

import javax.servlet.annotation.WebServlet

import ExampleObject.globalCnt
import org.vaadin.addons.vaactor._
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.{ BodySize, Push }
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.VaadinServletConfiguration
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo

import akka.actor.Actor.Receive
import akka.actor.{ Actor, Props }

/**
  * @author Otto Ringhofer
  */

object ExampleObject {
  // global counter
  private[this] var _globalCnt = 0

  def globalCnt: Int = this.synchronized { _globalCnt }

  def globalCnt_=(value: Int): Unit = this.synchronized { _globalCnt = value }

}

@WebServlet(
  urlPatterns = Array("/*"),
  asyncSupported = true
)
@VaadinServletConfiguration(
  productionMode = false
)
class ExampleServlet extends VaactorSessionServlet {

  override val sessionProps: Props = Props(classOf[ExampleSessionActor])

}

@BodySize(height = "100vh", width = "100vw")
@Route("")
@Theme(classOf[Lumo])
@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
class ExampleUI extends VerticalLayout with Vaactor.HasActor with Vaactor.HasSession {

  // counter local to this UI
  var uiCnt = 0

  val stateDisplay = new Label()

  setMargin(true)
  setSpacing(true)
  add(new Label("Vaactor Example") {
    // todo      addStyleName(ValoTheme.LABEL_H1)
  })
  add(new Button("Click Me", { _ =>
    uiCnt += 1
    session ! s"Thanks for clicking! (uiCnt:$uiCnt)"
  })
  )
  add(stateDisplay)
  add(new ExampleStateButton(this))

  override def receive: Receive = {
    case hello: String =>
      globalCnt += 1
      stateDisplay.setText(s"$hello (globalCnt:$globalCnt)")
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

class ExampleStateButton(val hasSession: Vaactor.HasSession)
  extends Button with Vaactor.HasActor {

  setText("SessionState")
  addClickListener { _ => hasSession.session ! VaactorSession.RequestSessionState }

  override def receive: Receive = {
    case state: Int => setText(s"SessionState is $state")
  }

}
