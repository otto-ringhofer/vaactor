package org.vaadin.addons

import com.typesafe.config.ConfigFactory

package object vaactor {
  val config = ConfigFactory.load()
  val vaactorConfig = config.getConfig("vaactor")
}
