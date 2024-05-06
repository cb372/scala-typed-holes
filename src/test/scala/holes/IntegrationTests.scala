package holes

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfterAll}
import org.scalatest.funspec.AnyFunSpec

import scala.sys.process._

class IntegrationTests extends AnyFunSpec with BeforeAndAfterAll {

  private val pluginJar = sys.props("plugin.jar")
  private val scalacClasspath = sys.props("scalac.classpath")
  private val targetDir = Paths.get("target/integration-tests")
  private def isScala3 = buildinfo.BuildInfo.scalaVersion.startsWith("3")

  private def runScalac(args: String*): String = {
    val buf = new StringBuffer
    val logger = new ProcessLogger {
      override def out(s: => String): Unit = { buf.append(s); buf.append('\n') }
      override def err(s: => String): Unit = { buf.append(s); buf.append('\n') }
      override def buffer[T](f: => T): T = f
    }

    val className =
      if (isScala3) "dotty.tools.dotc.Main"
      else "scala.tools.nsc.Main"

    Process(
      "java"
        :: "-Dscala.usejavacp=true"
        :: "-cp" :: scalacClasspath
        :: className
        :: args.filter(_.nonEmpty).toList
    ).!(logger)

    buf.toString
  }

  private def compileFile(path: Path): String =
    runScalac(
      s"-Xplugin:$pluginJar",
      "-P:typed-holes:log-level:info",
      if (isScala3) "-color:never" else "",
      if (isScala3) "-nowarn" else "",
      "-d",
      targetDir.toString,
      path.toString
    )

  override def beforeAll(): Unit = {
    println(runScalac("-version"))

    FileUtils.deleteQuietly(targetDir.toFile)
    Files.createDirectories(targetDir)
  }

  describe("produces the expected output") {
    for (
      scenario <- Paths
        .get("src/test/resources")
        .toFile
        .listFiles()
        .toList
        .map(_.toPath)
    ) {
      val expectedFileName = if (isScala3) "expected-3.txt" else "expected.txt"
      val expectedFile = scenario.resolve(expectedFileName)
      if (expectedFile.toFile.exists()) {
        it(scenario.getFileName.toString) {
          val expected =
            new String(
              Files.readAllBytes(expectedFile),
              StandardCharsets.UTF_8
            ).trim
          val actual =
            compileFile(scenario.resolve("input.scala")).trim

          if (actual != expected) {
            println("Compiler output:")
            println("=====")
            println(actual)
            println("=====")
          }
          assert(actual === expected)
        }
      }
    }
  }
}
