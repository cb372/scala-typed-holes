import sbt.Keys._
import sbt._
import ReleaseTransformations._
import sbtrelease.Vcs
import xerial.sbt.Sonatype._

scalacOptions ++= Seq("-deprecation")
libraryDependencies ++= Seq(
  scalaOrganization.value % "scala-compiler" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "commons-io" % "commons-io" % "2.6" % Test
)

organization := "com.github.cb372"
publishTo := sonatypePublishTo.value
crossVersion := CrossVersion.full
releaseCrossBuild := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
pomIncludeRepository := { _ => false }
publishMavenStyle := true
licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))
sonatypeProjectHosting := Some(GitHubHosting("cb372", "scala-typed-holes", "chris.birchall@gmail.com"))

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
    scalaVersion := "2.12.8",
    crossScalaVersions := Nil,
    publishArtifact := false,
    mdocVariables := Map("VERSION" -> version.value),
    mdocOut := file(".")
  )
  .enablePlugins(MdocPlugin)

val commitReadme: ReleaseStep = { state: State =>
  Vcs.detect(file(".")).foreach { vcs =>
    vcs.add("README.md") !! state.log
    vcs.commit(
      s"Update version in readme",
      sign = true,
      signOff = false
    ) !! state.log
  }

  state
}

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepInputTask(docs/mdoc),
  commitReadme,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)
