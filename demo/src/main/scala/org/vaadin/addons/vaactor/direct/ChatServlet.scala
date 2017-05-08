package org.vaadin.addons.vaactor.direct

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactor.VaactorServlet
import com.vaadin.annotations.VaadinServletConfiguration

/** Define servlet, url pattern and ui-class to start
  *
  * @author Otto Ringhofer
  */
@WebServlet(
  urlPatterns = Array("/direct/*"),
  asyncSupported = true
)
@VaadinServletConfiguration(
  productionMode = false,
  ui = classOf[ChatUI]
)
class ChatServlet extends VaactorServlet
