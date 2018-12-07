organization := "com.github.cb372"
libraryDependencies += scalaOrganization.value % "scala-compiler" % scalaVersion.value

publishTo := sonatypePublishTo.value
releaseCrossBuild := true
releasePublishArtifactsAction := PgpKeys.publishSigned.value
pomIncludeRepository := { _ => false }
publishMavenStyle := true
licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"))
homepage := Some(url("https://cb372.github.io/cats-retry/"))
developers := List(
  Developer(
    id    = "cb372",
    name  = "Chris Birchall",
    email = "chris.birchall@gmail.com",
    url   = url("https://github.com/cb372")
  )
)

scalacOptions ++= Seq("-deprecation")
scalacOptions in Test ++= {
  val jar = (packageBin in Compile).value
  Seq(s"-Xplugin:${jar.getAbsolutePath}", s"-Jdummy=${jar.lastModified}") // ensures recompile
}
