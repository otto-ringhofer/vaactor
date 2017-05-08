package org.vaadin.addons.vaactor

import Forwarder._
import com.vaadin.annotations.Push
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.ui.Transport
import com.vaadin.ui._

object TestUI {
  val TextName = "text"
  val ButtonName = "button"
  val AddComponentButtonName = "add-component"
  val RemoveComponentButtonName = "remove-component"
  val NameSuffix = "-dyn"
  val AddSubscriberButtonName = "add-subscriber"
  val RemoveSubscriberButtonName = "remove-subscriber"
}

@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
class TestUI extends VaactorUI {
  testUI =>

  import TestUI._

  val ap = "akka.actor.provider"
  val sn = "vaactor.system-name"


  override def init(request: VaadinRequest): Unit = {
    forwarder.tell(Register(UIActorName), uiActor)

    val layout = new VerticalLayout()
    var addedComponent: Component = null
    var addedSubscriber: Component = null

    val txt = new TextField {
      setWidth("100%")
      setId(TextName)
      setValue(s"$ap ${ loadedConfig.getString(ap) } $sn ${ loadedConfig.getString(sn) }")
    }

    val btn = new Button {
      setCaption("Send to session")
      setId(ButtonName)
      addClickListener { _ => send2SessionActor(TestServlet.SessionState(txt.getValue)) }
    }

    val addBtn = new Button {
      setCaption("Add component")
      setId(AddComponentButtonName)
      addClickListener { _ =>
        addedComponent = new TestComponent(testUI, NameSuffix)
        layout.addComponent(addedComponent)
      }
    }

    val remBtn = new Button {
      setCaption("Remove component")
      setId(RemoveComponentButtonName)
      addClickListener { _ => layout.removeComponent(addedComponent) }
    }

    val addSubsBtn = new Button {
      setCaption("Add subscriber")
      setId(AddSubscriberButtonName)
      addClickListener { _ =>
        addedSubscriber = new TestSubscriber(testUI)
        layout.addComponent(addedSubscriber)
      }
    }

    val remSubsBtn = new Button {
      setCaption("Remove subscriber")
      setId(RemoveSubscriberButtonName)
      addClickListener { _ => layout.removeComponent(addedSubscriber) }
    }

    val cmp = new TestComponent(testUI, "")

    layout.addComponents(txt, btn, cmp, addBtn, remBtn, addSubsBtn, remSubsBtn)
    setContent(layout)
  }

}
