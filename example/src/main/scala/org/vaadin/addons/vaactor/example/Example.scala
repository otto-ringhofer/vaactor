package org.vaadin.addons.vaactor.example

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactor.{ VaactorServlet, VaactorSession, VaactorUI }

import akka.actor.{ Actor, Props }
import vaadin.scala._
import vaadin.scala.server.ScaladinRequest

/**
  * @author Otto Ringhofer
  */

@WebServlet(urlPatterns = Array("/*"))
class ExampleServlet extends VaactorServlet(classOf[ExampleUI]) {

  override val sessionProps: Props = Props(classOf[ExampleSessionActor])

}

class ExampleUI extends VaactorUI {

  val layout = new VerticalLayout {
    margin = true
    spacing = true
    addComponent(new Label {
      value = "Vaactor Example"
      styleNames += ValoTheme.LabelH1
    })
    addComponent(Button("Click Me", { e =>
      vaactorUI.sessionActor ! "Thanks for clicking!"
    }))
  }

  override def initVaactorUI(request: ScaladinRequest): Unit = { content = layout }

  def receive = {
    case hello: String => layout.addComponent(Label(hello))
  }

}

class ExampleSessionActor extends Actor with VaactorSession[String] {

  override val initialSession = ""

  override val sessionBehaviour: Receive = {
    case name: String =>
      session = name
      sender ! s"Session received: $session"
  }

}
