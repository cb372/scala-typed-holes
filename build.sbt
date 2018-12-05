import ReleaseTransformations._
import xerial.sbt.Sonatype._

organization := "com.github.cb372"
scalaVersion := "2.12.8"
libraryDependencies += scalaOrganization.value % "scala-compiler" % scalaVersion.value

publishTo := sonatypePublishTo.value
releaseCrossBuild := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
pomIncludeRepository := { _ => false }
publishMavenStyle := true
licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))
sonatypeProjectHosting := Some(GitHubHosting("cb372", "scala-typed-holes", "chris.birchall@gmail.com"))

scalacOptions in Test ++= {
  val jar = (packageBin in Compile).value
  Seq(s"-Xplugin:${jar.getAbsolutePath}", s"-Jdummy=${jar.lastModified}") // ensures recompile
}

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)
