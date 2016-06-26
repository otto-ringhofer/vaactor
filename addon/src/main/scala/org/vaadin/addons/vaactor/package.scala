package org.vaadin.addons

import com.typesafe.config.ConfigFactory

package object vaactor {
  val loadedConfig = ConfigFactory.load()
  val config = loadedConfig.getConfig("vaactor")
}
