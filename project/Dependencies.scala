import sbt._

object Dependencies {

  val vaadinVersion = "8.0.6"
  val servletapiVersion = "3.1.0"
  val slf4jVersion = "1.7.25"
  val configVersion = "1.3.1"
  val akkaVersion = "2.5.1"
  val scalatestVersion = "3.0.1"
  val seleniumVersion = "3.4.0"

  val akkaOrg = "com.typesafe.akka"
  val slf4jOrg = "org.slf4j"
  val vaadinOrg = "com.vaadin"

  val servletApi: ModuleID = "javax.servlet" % "javax.servlet-api" % servletapiVersion % "provided"
  val config: ModuleID = "com.typesafe" % "config" % configVersion
  val vaadinServer: ModuleID = vaadinOrg % "vaadin-server" % vaadinVersion
  val vaadinClientCompiled: ModuleID = vaadinOrg % "vaadin-client-compiled" % vaadinVersion
  val vaadinThemes: ModuleID = vaadinOrg % "vaadin-themes" % vaadinVersion
  val vaadinPush: ModuleID = vaadinOrg % "vaadin-push" % vaadinVersion
  val slf4j: ModuleID = slf4jOrg % "slf4j-api" % slf4jVersion % "provided"
  val slf4jSimple: ModuleID = slf4jOrg % "slf4j-simple" % slf4jVersion
  val akkaActor: ModuleID = akkaOrg %% "akka-actor" % akkaVersion
  val akkaSlf4j: ModuleID = akkaOrg %% "akka-slf4j" % akkaVersion
  val akkaRemote: ModuleID = akkaOrg %% "akka-remote" % akkaVersion
  val scalactic: ModuleID = "org.scalactic" %% "scalactic" % scalatestVersion % "test"
  val scalatest: ModuleID = "org.scalatest" %% "scalatest" % scalatestVersion % "test"
  val akkaTestkit: ModuleID = akkaOrg %% "akka-testkit" % akkaVersion % "test"
  val seleniumJava: ModuleID = "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion % "test"

  val vaadinServletDeps = Seq(
    servletApi,
    slf4jSimple,
    vaadinClientCompiled,
    vaadinThemes
  )

  val addonDeps = Seq(
    config,
    akkaActor,
    slf4j,
    akkaSlf4j,
    vaadinServer,
    vaadinPush,
    servletApi,
    scalactic,
    scalatest,
    akkaTestkit
  )

  val exampleDeps: Seq[ModuleID] = vaadinServletDeps

  val demoDeps: Seq[ModuleID] = vaadinServletDeps

  val testDeps: Seq[ModuleID] = Seq(
    akkaRemote,
    scalactic,
    scalatest,
    akkaTestkit,
    seleniumJava
  ) ++ vaadinServletDeps

}
