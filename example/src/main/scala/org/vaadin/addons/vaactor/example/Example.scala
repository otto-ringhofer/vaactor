package org.vaadin.addons.vaactor.example

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactor.{ VaactorServlet, VaactorSession, VaactorUI }
import com.vaadin.server.VaadinRequest
import com.vaadin.ui._
import com.vaadin.ui.themes.ValoTheme

import akka.actor.{ Actor, Props }

/**
  * @author Otto Ringhofer
  */

@WebServlet(urlPatterns = Array("/*"))
class ExampleServlet extends VaactorServlet(classOf[ExampleUI]) {

  override val sessionProps: Props = Props(classOf[ExampleSessionActor])

}

class ExampleUI extends VaactorUI {

  val layout = new VerticalLayout {
    setMargin(true)
    setSpacing(true)
    addComponent(new Label {
      setValue("Vaactor Example")
      addStyleName(ValoTheme.LABEL_H1)
    })
    addComponent(new Button("Click Me", _ => vaactorUI.sessionActor ! "Thanks for clicking!"))
  }

  override def initVaactorUI(request: VaadinRequest): Unit = { setContent(layout) }

  def receive: PartialFunction[Any, Unit] = {
    case hello: String => layout.addComponent(new Label(hello))
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
