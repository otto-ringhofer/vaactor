name := "vaactor"

lazy val root = project.in(file("."))
  .settings(inThisBuild(Seq(
    organization := "org.vaadin.addons",
    version := "1.0.0-SNAPSHOT", // change also in reference.conf
    scalaVersion := "2.12.2",
    crossScalaVersions := Seq("2.12.2"),
    scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation"),
    scalacOptions in(Compile, doc) ++= Seq("-groups", "-implicits", "-diagrams"),
    resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases",
    commands += Command.command("testAll") { state =>
      "project addon" ::
        "test" ::
        "project test" ::
        "jetty:start" ::
        "testOnly at.co.sdt.vaadin.VaactorServletSpec" ::
        "testOnly at.co.sdt.vaadin.VaactorUISpec" ::
        "testOnly at.co.sdt.vaadin.VaactorSpec" ::
        "jetty:stop" ::
        "project root" ::
        state
    }
  )))
  .aggregate(addon, demo, chat, example, test)

lazy val addon = (project in file("addon"))
  .settings(
    name := "vaactor",
    libraryDependencies := Dependencies.addonDeps
  )

lazy val demo = (project in file("demo"))
  .enablePlugins(JettyPlugin)
  .settings(
    name := "vaactor-demo",
    libraryDependencies ++= Dependencies.demoDeps
  ).dependsOn(addon)

lazy val chat = (project in file("chat"))
  .enablePlugins(JettyPlugin)
  .settings(
    name := "vaactor-chat",
    libraryDependencies ++= Dependencies.chatDeps
  ).dependsOn(addon)

lazy val example = (project in file("example"))
  .enablePlugins(JettyPlugin)
  .settings(
    name := "vaactor-example",
    libraryDependencies ++= Dependencies.exampleDeps
  ).dependsOn(addon)

lazy val test = (project in file("test"))
  .enablePlugins(JettyPlugin)
  .settings(
    name := "vaactor-test",
    libraryDependencies ++= Dependencies.exampleDeps,
    parallelExecution in Test := false
  ).dependsOn(addon)
