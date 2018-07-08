package org.vaadin.addons

import com.typesafe.config.{ Config, ConfigFactory }

/** Library supports usage of [[https://vaadin.com/flow Vaadin]] with [[http://akka.io/ Akka]] actors.
  *
  * @author Otto Ringhofer
  */
package object vaactor {

  /** Complete configuration loaded from config files */
  val loadedConfig: Config = ConfigFactory.load()

  /** Vaactor configuration - subtree */
  val config: Config = loadedConfig.getConfig("vaactor")

}
