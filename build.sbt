name := BuildConfig.name

resolvers += "Vaadin prereleases" at "https://maven.vaadin.com/vaadin-prereleases"
resolvers += "Vaadin Directory" at "http://maven.vaadin.com/vaadin-addons"

lazy val root = project.in(file("."))
  .settings(inThisBuild(Seq(
    organization := BuildConfig.organization,
    version := BuildConfig.version,
    scalaVersion := ScalaConfig.version,
    crossScalaVersions := ScalaConfig.crossVersions,
    scalacOptions ++= ScalaConfig.compileOptions,
    scalacOptions in(Compile, doc) ++= ScalaConfig.docCompileOptions,
    commands ++= Commands.allCommands
  )))
  .aggregate(addon, example, demo, test)

lazy val addon = (project in file("addon"))
  .settings(
    name := BuildConfig.name,
    description := BuildConfig.description,
    libraryDependencies := Dependencies.addonDeps,
    packageOptions in(Compile, packageBin) ++= BuildConfig.packageOptions
  )

lazy val demo = (project in file("demo"))
  .enablePlugins(JettyPlugin)
  .settings(
    name := s"${ BuildConfig.name }-demo",
    libraryDependencies ++= Dependencies.demoDeps,
    containerLibs in Jetty := Seq(Dependencies.jettyLib),
  ).dependsOn(addon)

lazy val example = (project in file("example"))
  .enablePlugins(JettyPlugin)
  .settings(
    name := s"${ BuildConfig.name }-example",
    libraryDependencies ++= Dependencies.exampleDeps,
    containerLibs in Jetty := Seq(Dependencies.jettyLib),
  ).dependsOn(addon)

lazy val test = (project in file("test"))
  .enablePlugins(JettyPlugin)
  .settings(
    name := s"${ BuildConfig.name }-test",
    libraryDependencies ++= Dependencies.testDeps,
    containerLibs in Jetty := Seq(Dependencies.jettyLib),
    parallelExecution in Test := false
  ).dependsOn(addon)
