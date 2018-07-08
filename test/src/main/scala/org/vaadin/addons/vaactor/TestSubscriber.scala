package org.vaadin.addons.vaactor

import TestServlet._
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.orderedlayout.VerticalLayout

class TestSubscriber
  extends VerticalLayout with Vaactor.HasSession with Vaactor.AttachSession {

  override val attachMessage: Any = Attach
  override val detachMessage: Any = Detach

  add(new Label("I'm here"))

}
