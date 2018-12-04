scalaVersion := "2.12.8"
libraryDependencies += scalaOrganization.value % "scala-compiler" % scalaVersion.value
scalacOptions in Test ++= {
  val jar = (packageBin in Compile).value
  Seq(s"-Xplugin:${jar.getAbsolutePath}", s"-Jdummy=${jar.lastModified}") // ensures recompile
}
