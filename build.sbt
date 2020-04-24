import sbt.Keys._
import sbt._

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / crossScalaVersions := Seq(
  //"2.11.11",
  //"2.12.8",
  //"2.12.9",
  //"2.12.10",
  //"2.12.11",
  //"2.13.0",
  //"2.13.1",
  "2.13.2"
)
ThisBuild / githubWorkflowJavaVersions := Seq(
  "adopt@1.8",
  "adopt@1.9",
  "adopt@1.10",
  "adopt@1.11",
  "adopt@1.12",
  "adopt@1.13",
  "adopt@1.14",
  "openjdk@1.9",
  "openjdk@1.10",
  "openjdk@1.11",
  "openjdk@1.12",
  "openjdk@1.13",
  "openjdk@1.14"
)
ThisBuild / githubWorkflowBuild := WorkflowStep.Sbt(List("test", "docs/mdoc"))

scalacOptions ++= Seq("-deprecation")
libraryDependencies ++= Seq(
  scalaOrganization.value % "scala-compiler" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "commons-io" % "commons-io" % "2.6" % Test
)

organization := "com.github.cb372"
crossVersion := CrossVersion.full
pomIncludeRepository := { _ => false }
publishMavenStyle := true
licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))

fork in Test := true
javaOptions in Test ++= {
  val jar = (packageBin in Compile).value
  val scalacClasspath = scalaInstance.value.allJars.mkString(java.io.File.pathSeparator)
  Seq(
    s"-Dplugin.jar=${jar.getAbsolutePath}",
    s"-Dscalac.classpath=$scalacClasspath"
  )
}

val `scala-typed-holes` = project.in(file("."))

val docs = project
  .in(file("generated-docs")) // important: it must not be the actual directory name, i.e. docs/
  .settings(
    crossScalaVersions := Nil,
    publishArtifact := false,
    mdocVariables := Map("VERSION" -> version.value),
    mdocOut := file(".")
  )
  .enablePlugins(MdocPlugin)
