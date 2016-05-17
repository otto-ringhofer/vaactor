import sbt._

object Dependencies {

  private val vaadinVersion = "7.5.10"

  private val vaadin = "com.vaadin" % "vaadin-server" % vaadinVersion
  private val vaadinClientCompiled = "com.vaadin" % "vaadin-client-compiled" % vaadinVersion
  private val vaadinThemes = "com.vaadin" % "vaadin-themes" % vaadinVersion
  private val vaadinPush = "com.vaadin" % "vaadin-push" % vaadinVersion
  private val servletApi = "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
  private val akkaActor = "com.typesafe.akka" %% "akka-actor" % "2.4.4"
  private val scaladin = "org.vaadin.addons" %% "scaladin" % "3.2-SNAPSHOT"
  private val typesafeConfig = "com.typesafe" % "config" % "1.3.0"

  val addonDeps = Seq(
    vaadin,
    vaadinPush,
    servletApi,
    akkaActor,
    typesafeConfig,
    scaladin
  )

  val demoDeps = Seq(
    vaadinClientCompiled,
    vaadinThemes
  )
}
