import sbt._

object Dependencies {

  val vaadinVersion = "8.3.1"
  val servletapiVersion = "3.1.0"
  val slf4jVersion = "1.7.25"
  val akkaVersion = "2.5.11"
  val scalatestVersion = "3.0.5"
  val seleniumVersion = "3.10.0"

  val akkaOrg = "com.typesafe.akka"
  val slf4jOrg = "org.slf4j"
  val vaadinOrg = "com.vaadin"

  val servletApi = "javax.servlet" % "javax.servlet-api" % servletapiVersion % "provided"
  val vaadinServer = vaadinOrg % "vaadin-server" % vaadinVersion
  val vaadinClientCompiled = vaadinOrg % "vaadin-client-compiled" % vaadinVersion
  val vaadinThemes = vaadinOrg % "vaadin-themes" % vaadinVersion
  val vaadinPush = vaadinOrg % "vaadin-push" % vaadinVersion
  val slf4j = slf4jOrg % "slf4j-api" % slf4jVersion % "provided"
  val slf4jSimple = slf4jOrg % "slf4j-simple" % slf4jVersion
  val akkaActor = akkaOrg %% "akka-actor" % akkaVersion
  val akkaSlf4j = akkaOrg %% "akka-slf4j" % akkaVersion
  val akkaRemote = akkaOrg %% "akka-remote" % akkaVersion
  val scalactic = "org.scalactic" %% "scalactic" % scalatestVersion % "test"
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion % "test"
  val akkaTestkit = akkaOrg %% "akka-testkit" % akkaVersion % "test"
  val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion % "test"

  val addonDeps = Seq(
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

  val vaadinServletDeps: Seq[ModuleID] = addonDeps ++ Seq(
    servletApi,
    slf4jSimple,
    vaadinClientCompiled,
    vaadinThemes
  )

  val exampleDeps: Seq[ModuleID] = vaadinServletDeps

  val demoDeps: Seq[ModuleID] = vaadinServletDeps

  val testDeps: Seq[ModuleID] = vaadinServletDeps ++ Seq(
    akkaRemote,
    scalactic,
    scalatest,
    akkaTestkit,
    seleniumJava
  )

  //noinspection Annotator
  // Vaadin 8.1 has problems with default jetty in plugin :-(
  // https://mvnrepository.com/artifact/org.eclipse.jetty/jetty-runner
  val jettyLib = "org.eclipse.jetty" % "jetty-runner" % "9.3.21.v20170918" intransitive()
  val jettyMain = "org.eclipse.jetty.runner.Runner"

}
