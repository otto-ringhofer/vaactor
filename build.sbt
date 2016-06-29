name := "vaactor"

organization := "org.vaadin.addons"

version in ThisBuild := "0.0.1"

scalaVersion in ThisBuild := "2.11.8"

crossScalaVersions in ThisBuild := Seq("2.11.8")

scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature")

resolvers in ThisBuild += "Scaladin Snapshots" at "http://henrikerola.github.io/repository/snapshots/"

lazy val root = project.in(file(".")).aggregate(addon, demo)

lazy val addon = project
  .settings(
    name := "vaactor",
    libraryDependencies := Dependencies.addonDeps
  )

lazy val demo = project
  .enablePlugins(JettyPlugin)
  .settings(
    name := "vaactor-demo",
    libraryDependencies ++= Dependencies.demoDeps
  ).dependsOn(addon)

lazy val example = project
  .enablePlugins(JettyPlugin)
  .settings(
    name := "vaactor-example",
    libraryDependencies ++= Dependencies.exampleDeps
  ).dependsOn(addon)
