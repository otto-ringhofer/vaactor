name := "vaactor"

lazy val root = project.in(file("."))
  .settings(inThisBuild(Seq(
    organization := "org.vaadin.addons",
    version := "1.0.0-SNAPSHOT", // change also in reference.conf and MANIFEST.MF
    scalaVersion := "2.12.3",
    crossScalaVersions := Seq("2.12.3"),
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),
    scalacOptions in(Compile, doc) ++= Seq("-groups", "-implicits", "-diagrams"),
    commands ++= Commands.allCommands
  )))
  .aggregate(addon, example, demo, test)

lazy val addon = (project in file("addon"))
  .settings(
    name := "vaactor",
    description := "Vaactor bridges the gap between Vaadin Servlet and Akka Actors",
    libraryDependencies := Dependencies.addonDeps
  )

lazy val demo = (project in file("demo"))
  .enablePlugins(JettyPlugin)
  .settings(
    name := "vaactor-demo",
    libraryDependencies ++= Dependencies.demoDeps,
    containerLibs in Jetty := Seq(Dependencies.jettyLib),
    containerMain in Jetty := Dependencies.jettyMain
  ).dependsOn(addon)

lazy val example = (project in file("example"))
  .enablePlugins(JettyPlugin)
  .settings(
    name := "vaactor-example",
    libraryDependencies ++= Dependencies.exampleDeps,
    containerLibs in Jetty := Seq(Dependencies.jettyLib),
    containerMain in Jetty := Dependencies.jettyMain
  ).dependsOn(addon)

lazy val test = (project in file("test"))
  .enablePlugins(JettyPlugin)
  .settings(
    name := "vaactor-test",
    libraryDependencies ++= Dependencies.testDeps,
    containerLibs in Jetty := Seq(Dependencies.jettyLib),
    containerMain in Jetty := Dependencies.jettyMain,
    parallelExecution in Test := false
  ).dependsOn(addon)
