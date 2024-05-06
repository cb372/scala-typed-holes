import sbt.Keys._
import sbt._

scalaVersion := "2.13.4"
crossScalaVersions := List(
  "2.11.12",
  "2.12.15",
  "2.13.0",
  "2.13.1",
  "2.13.2",
  "2.13.3",
  "2.13.4",
  "2.13.5",
  "2.13.6",
  "2.13.7",
  "2.13.8",
  "3.3.3",
  "3.4.1"
)

crossVersion := CrossVersion.full
scalacOptions ++= Seq("-deprecation")

libraryDependencies ++= Seq(
  scalaVersion.value match {
    case version if version.startsWith("3.") =>
      "org.scala-lang" %% "scala3-compiler" % scalaVersion.value
    case _ =>
      scalaOrganization.value % "scala-compiler" % scalaVersion.value
  },
  "org.scalatest" %% "scalatest" % "3.2.18" % Test,
  "commons-io" % "commons-io" % "2.6" % Test
)

organization := "com.github.cb372"
homepage := Some(url("https://github.com/cb372/scala-typed-holes"))
licenses := Seq(
  "Apache License, Version 2.0" -> url(
    "http://www.apache.org/licenses/LICENSE-2.0.html"
  )
)
developers := List(
  Developer(
    "cb372",
    "Chris Birchall",
    "chris.birchall@gmail.com",
    url("https://twitter.com/cbirchall")
  )
)

Test / fork := true
Test / javaOptions ++= {
  val jar = (Compile / packageBin).value
  val scalacClasspath =
    scalaInstance.value.allJars.mkString(java.io.File.pathSeparator)
  Seq(
    s"-Dplugin.jar=${jar.getAbsolutePath}",
    s"-Dscalac.classpath=$scalacClasspath"
  )
}

val `scala-typed-holes` =
  project
    .in(file("."))
    .enablePlugins(BuildInfoPlugin)

val `scala-typed-holes-3` = // just for IDE support
  project
    .in(file("scala-typed-holes-3"))
    .settings(
      scalaVersion := "3.4.1",
      crossScalaVersions := Nil,
      Compile / unmanagedSourceDirectories := Seq(),
      Compile / unmanagedSourceDirectories += (ThisBuild / baseDirectory).value / "src" / "main" / "scala",
      Compile / unmanagedSourceDirectories += (ThisBuild / baseDirectory).value / "src" / "main" / "scala-3",
      moduleName := "scala-typed-holes-3",
      target := (ThisBuild / baseDirectory).value / "target" / "target3",
      publish / skip := true,
      libraryDependencies ++= Seq(
        "org.scala-lang" %% "scala3-compiler" % "3.4.1"
      )
    )

val docs = project
  .in(
    file("generated-docs")
  ) // important: it must not be the actual directory name, i.e. docs/
  .settings(
    scalaVersion := "2.12.10",
    crossScalaVersions := Nil,
    publishArtifact := false,
    publish / skip := true,
    mdocVariables := Map("VERSION" -> version.value),
    mdocOut := file(".")
  )
  .enablePlugins(MdocPlugin)
