package org.vaadin.addons

import com.typesafe.config.ConfigFactory

/** library supports usage of actors with vaadin
  *
  * @author Otto Ringhofer
  */
package object vaactor {

  /** complete configuration loaded from config files */
  val loadedConfig = ConfigFactory.load()

  /** vaactor configuration - subtree */
  val config = loadedConfig.getConfig("vaactor")

}
