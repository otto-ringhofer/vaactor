package org.vaadin.addons.vaactor

import Forwarder._
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport

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


  override def initContent(): Component = {
    forwarder.tell(Register(UIActorName), uiActor)

    val layout = new VerticalLayout()
    var addedComponent: Component = null
    var addedSubscriber: Component = null

    val txt = new TextField {
      setWidth("100%")
      setId(TextName)
      setValue(s"$ap ${ loadedConfig.getString(ap) } $sn ${ loadedConfig.getString(sn) }")
    }

    val btn = new Button("Send to session") {
      setId(ButtonName)
      addClickListener { _ => send2SessionActor(TestServlet.SessionState(txt.getValue)) }
    }

    val addBtn = new Button("Add component") {
      setId(AddComponentButtonName)
      addClickListener { _ =>
        addedComponent = new TestComponent(testUI, NameSuffix)
        layout.add(addedComponent)
      }
    }

    val remBtn = new Button("Remove component") {
      setId(RemoveComponentButtonName)
      addClickListener { _ => layout.remove(addedComponent) }
    }

    val addSubsBtn = new Button("Add subscriber") {
      setId(AddSubscriberButtonName)
      addClickListener { _ =>
        addedSubscriber = new TestSubscriber(testUI)
        layout.add(addedSubscriber)
      }
    }

    val remSubsBtn = new Button("Remove subscriber") {
      setId(RemoveSubscriberButtonName)
      addClickListener { _ => layout.remove(addedSubscriber) }
    }

    val cmp = new TestComponent(testUI, "")

    layout.add(txt, btn, cmp, addBtn, remBtn, addSubsBtn, remSubsBtn)
    layout
  }

}
