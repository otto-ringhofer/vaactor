package org.vaadin.addons

import com.typesafe.config.{ Config, ConfigFactory }

/** library supports usage of actors with vaadin
  *
  * @author Otto Ringhofer
  */
package object vaactor {

  /** complete configuration loaded from config files */
  val loadedConfig: Config = ConfigFactory.load()

  /** vaactor configuration - subtree */
  val config: Config = loadedConfig.getConfig("vaactor")

}
