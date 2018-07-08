import sbt._

object BuildConfig {

  val version = "2.0.0" // change also in reference.conf

  val organization = "org.vaadin.addons"

  val name = "vaactor"

  val description = "Vaactor bridges the gap between Vaadin Servlet and Akka Actors"

  val packageOptions = Seq(
    Package.ManifestAttributes("Vaadin-Package-Version" -> "1"),
    Package.ManifestAttributes("Vaadin-Addon" -> s"vaactor_${ ScalaConfig.scalaBinVersion }-${ version }.jar"),
    Package.ManifestAttributes("Implementation-Title" -> "Vaactor"),
    Package.ManifestAttributes("Implementation-Vendor" -> "Otto Ringhofer"),
    Package.ManifestAttributes("Specification-Title" -> "Vaactor"),
    Package.ManifestAttributes("Specification-Vendor" -> "Otto Ringhofer"),
    Package.ManifestAttributes("Vaadin-License-Title" -> "Apache License 2.0")
  )
}
