name := "Vaactors"

organization := "org.vaadin.addons"

version in ThisBuild := "0.0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

crossScalaVersions in ThisBuild := Seq("2.11.8")

scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-encoding", "UTF-8")

resolvers in ThisBuild += "Scaladin Snapshots" at "http://henrikerola.github.io/repository/snapshots/"

lazy val root = project.in(file(".")).aggregate(addon, demo)

lazy val addon = project
  .settings(
    name := "Vaactors",
    libraryDependencies := Dependencies.addonDeps
  )

lazy val demo = project
  .enablePlugins(JettyPlugin)
  .settings(
    name := "vaactors-demo",
    libraryDependencies ++= Dependencies.demoDeps
  ).dependsOn(addon)
