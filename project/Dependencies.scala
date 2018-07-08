import sbt._

object Dependencies {

  val vaadinVersion = "10.0.1"
  val servletapiVersion = "3.1.0"
  val slf4jVersion = "1.7.25"
  val akkaVersion = "2.5.13"
  val scalatestVersion = "3.0.5"
  val seleniumVersion = "3.10.0"

  val akkaOrg = "com.typesafe.akka"
  val slf4jOrg = "org.slf4j"
  val vaadinOrg = "com.vaadin"
  val vaadinWebjarsOrg = "org.webjars.bowergithub.vaadin"

  val vaadinCore = vaadinOrg % "vaadin-core" % vaadinVersion
  val servletApi = "javax.servlet" % "javax.servlet-api" % servletapiVersion % "provided"
  val slf4j = slf4jOrg % "slf4j-api" % slf4jVersion % "provided"
  val slf4jSimple = slf4jOrg % "slf4j-simple" % slf4jVersion
  val akkaActor = akkaOrg %% "akka-actor" % akkaVersion
  val akkaSlf4j = akkaOrg %% "akka-slf4j" % akkaVersion
  val akkaRemote = akkaOrg %% "akka-remote" % akkaVersion
  val scalactic = "org.scalactic" %% "scalactic" % scalatestVersion % "test"
  val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion % "test"
  val akkaTestkit = akkaOrg %% "akka-testkit" % akkaVersion % "test"
  val seleniumJava = "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion % "test"

  val addonDeps: Seq[ModuleID] = Seq(
    vaadinCore,
    akkaActor,
    slf4j,
    akkaSlf4j,
    servletApi,
    scalactic,
    scalatest,
    akkaTestkit
  )

  val vaadinServletDeps: Seq[ModuleID] = addonDeps ++ Seq(
    servletApi,
    slf4jSimple
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

}
