package org.vaadin.addons.vaactor

import Forwarder._
import TestUI._
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
}

@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
class TestUI extends VaactorUI {
  val ap = "akka.actor.provider"
  val sn = "vaactor.system-name"

  override def init(request: VaadinRequest): Unit = {
    forwarder.tell(Register(UIActorName), uiActor)

    val layout = new VerticalLayout()
    var addedComponent: Component = null

    val txt = new TextField()
    txt.setWidth("100%")
    txt.setId(TextName)
    txt.setValue(s"$ap ${ loadedConfig.getString(ap) } $sn ${ loadedConfig.getString(sn) }")

    val btn = new Button("Send to session")
    btn.setId(ButtonName)
    btn.addClickListener { _ => send2SessionActor(TestServlet.SessionState(txt.getValue)) }

    val addBtn = new Button("Add component")
    addBtn.setId(AddComponentButtonName)
    addBtn.addClickListener { _ =>
      addedComponent = new TestComponent(this, NameSuffix)
      layout.addComponent(addedComponent)
    }

    val remBtn = new Button("Remove component")
    remBtn.setId(RemoveComponentButtonName)
    remBtn.addClickListener { _ => layout.removeComponent(addedComponent) }

    val cmp = new TestComponent(this, "")

    layout.addComponents(txt, btn, cmp, addBtn, remBtn)
    setContent(layout)
  }

}
