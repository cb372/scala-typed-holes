import sbt.Keys._
import sbt._

crossVersion := CrossVersion.full
scalacOptions ++= Seq("-deprecation")
libraryDependencies ++= Seq(
  scalaOrganization.value % "scala-compiler" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "commons-io" % "commons-io" % "2.6" % Test
)

organization := "com.github.cb372"
licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))
developers := List(
  Developer(
    "cb372",
    "Chris Birchall",
    "chris.birchall@gmail.com",
    url("https://twitter.com/cbirchall")
  )
)

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
    scalaVersion := "2.12.10",
    crossScalaVersions := Nil,
    publishArtifact := false,
    skip in publish := true,
    mdocVariables := Map("VERSION" -> version.value),
    mdocOut := file(".")
  )
  .enablePlugins(MdocPlugin)

//val commitReadme: ReleaseStep = { state: State =>
  //Vcs.detect(file(".")).foreach { vcs =>
    //vcs.add("README.md") !! state.log
    //vcs.commit(
      //s"Update version in readme",
      //sign = true,
      //signOff = false
    //) !! state.log
  //}

  //state
//}

//releaseProcess := Seq[ReleaseStep](
  //checkSnapshotDependencies,
  //inquireVersions,
  //runClean,
  //runTest,
  //setReleaseVersion,
  //commitReleaseVersion,
  //tagRelease,
  //releaseStepInputTask(docs/mdoc),
  //commitReadme,
  //publishArtifacts,
  //setNextVersion,
  //commitNextVersion,
  //releaseStepCommand("sonatypeReleaseAll"),
  //pushChanges
//)
