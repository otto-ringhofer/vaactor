name := "vaactor"

organization := "org.vaadin.addons"

version in ThisBuild := "1.0.0-SNAPSHOT" // change also in reference.conf

scalaVersion in ThisBuild := "2.12.1"

crossScalaVersions in ThisBuild := Seq("2.12.1")

scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature")

lazy val root = project.in(file(".")).aggregate(addon, demo, example)

lazy val addon = project
  .settings(
    name := "vaactor",
    libraryDependencies := Dependencies.addonDeps
  )

lazy val demo = project
  .enablePlugins(TomcatPlugin)
  .settings(
    name := "vaactor-demo",
    javaOptions in Tomcat ++= Dependencies.javaOptionsInTomcat,
    libraryDependencies ++= Dependencies.demoDeps
  ).dependsOn(addon)

lazy val example = project
  .enablePlugins(TomcatPlugin)
  .settings(
    name := "vaactor-example",
    javaOptions in Tomcat ++= Dependencies.javaOptionsInTomcat,
    libraryDependencies ++= Dependencies.exampleDeps
  ).dependsOn(addon)
