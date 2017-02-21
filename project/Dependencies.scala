import sbt._

object Dependencies {

  val vaadinVersion = "8.0.0.rc2"
  val akkaVersion = "2.4.17"

  val vaadinServer: ModuleID = "com.vaadin" % "vaadin-server" % vaadinVersion
  val vaadinClientCompiled: ModuleID = "com.vaadin" % "vaadin-client-compiled" % vaadinVersion
  val vaadinThemes: ModuleID = "com.vaadin" % "vaadin-themes" % vaadinVersion
  val vaadinPush: ModuleID = "com.vaadin" % "vaadin-push" % vaadinVersion
  val servletApi: ModuleID = "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided"
  val akkaActor: ModuleID = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val typesafeConfig: ModuleID = "com.typesafe" % "config" % "1.3.1"
  val scalatest: ModuleID = "org.scalatest" % "scalatest_2.11" % "3.0.1" % "test"
  val akkaTestkit: ModuleID = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"

  val addonDeps = Seq(
    vaadinServer,
    vaadinPush,
    servletApi,
    akkaActor,
    typesafeConfig,
    scalatest,
    akkaTestkit
  )

  val demoDeps = Seq(
    vaadinClientCompiled,
    vaadinThemes,
    servletApi
  )

  val exampleDeps = Seq(
    vaadinClientCompiled,
    vaadinThemes,
    servletApi
  )

  val javaOptionsInTomcat = Seq(
    "-Xdebug",
    "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
  )

}
