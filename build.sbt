import ReleaseTransformations._
import xerial.sbt.Sonatype._

scalacOptions ++= Seq("-deprecation")
libraryDependencies ++= Seq(
  scalaOrganization.value % "scala-compiler" % scalaVersion.value,
  "org.scalatest" %% "scalatest" % "3.0.6-SNAP5" % Test,
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

//scalacOptions in Test ++= {
//  val jar = (packageBin in Compile).value
//  Seq(
//    s"-Xplugin:${jar.getAbsolutePath}",
//    s"-Jdummy=${jar.lastModified}", // ensures recompile
//    "-P:typed-holes:log-level:info"
//  )
//}
fork in Test := true
javaOptions in Test ++= {
  val jar = (packageBin in Compile).value
  val scalacClasspath = scalaInstance.value.allJars.mkString(java.io.File.pathSeparator)
  Seq(
    s"-Dplugin.jar=${jar.getAbsolutePath}",
    s"-Dscalac.classpath=$scalacClasspath"
//    "-P:typed-holes:log-level:info"
  )
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
