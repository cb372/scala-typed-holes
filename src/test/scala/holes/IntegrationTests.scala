package holes

import buildinfo.BuildInfo.scalaVersion
import org.apache.commons.io.FileUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import scala.sys.process._

class IntegrationTests extends AnyFunSpec with BeforeAndAfterAll {

  private val pluginJar = sys.props("plugin.jar")
  private val scalacClasspath = sys.props("scalac.classpath")
  private val targetDir = Paths.get("target/integration-tests")
  private def isScala3 = scalaVersion.startsWith("3")

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
      expectFile(scalaVersion, scenario) match {
        case Some(expectedFile) =>
          it(scenario.getFileName.toString) {
            val expected =
              new String(
                Files.readAllBytes(expectedFile),
                StandardCharsets.UTF_8
              ).trim
            val actual =
              compileFile(scenario.resolve("input.scala")).trim

            if (actual != expected) {
              val tmpActual = Files.createTempFile("actual", ".txt")
              Files.write(tmpActual, actual.getBytes(StandardCharsets.UTF_8))
              println("Compiler output:")
              println(s"Written to: $tmpActual")
              println("=====")
              println(actual)
              println("=====")
              println("Expected output:")
              println("=====")
              println(expected)
              println("=====")
              println(s"Output written to $tmpActual")
            }
            assert(actual === expected)
          }
        case None =>
          ignore(scenario.getFileName.toString) {}
      }
    }
  }

  private def expectFile(scalaVersion: String, scenario: Path) = {
    val possibleNames = scalaVersion.split('.') match {
      case Array("3", m, _) =>
        List(
          s"expected-3.$m.txt",
          s"expected-3.txt"
        )
      case Array("2", mi, _) => List(s"expected.txt")
      case _                 => Nil
    }
    possibleNames.collectFirst {
      case fileName if scenario.resolve(fileName).toFile.exists() =>
        scenario.resolve(fileName)
    }
  }
}
