package holes

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

import org.apache.commons.io.FileUtils
import org.scalatest.{BeforeAndAfterAll, FunSpec}

import scala.sys.process._

class IntegrationTests extends FunSpec with BeforeAndAfterAll {

  private val pluginJar = sys.props("plugin.jar")
  private val scalacClasspath = sys.props("scalac.classpath")
  private val targetDir = Paths.get("target/integration-tests")

  private def runScalac(args: String*): String = {
    val buf = new StringBuffer
    val logger = new ProcessLogger {
      override def out(s: => String): Unit = { buf.append(s); buf.append('\n') }
      override def err(s: => String): Unit = { buf.append(s); buf.append('\n') }
      override def buffer[T](f: => T): T = f
    }

    Process(
      "java"
        :: "-Dscala.usejavacp=true"
        :: "-cp" :: scalacClasspath
        :: "scala.tools.nsc.Main"
        :: args.toList
    ).!(logger)

    buf.toString
  }

  private def compileFile(path: Path): String =
    runScalac(
      s"-Xplugin:$pluginJar",
      "-P:typed-holes:log-level:info",
      "-d", targetDir.toString,
      path.toString
    )

  override def beforeAll(): Unit = {
    println(runScalac("-version"))

    FileUtils.deleteQuietly(targetDir.toFile)
    Files.createDirectories(targetDir)
  }

  describe("produces the expected output") {
    for (scenario <- Paths.get("src/test/resources").toFile.listFiles().toList.map(_.toPath)) {
      it(scenario.getFileName.toString) {
        val expected =
          new String(Files.readAllBytes(scenario.resolve("expected.txt")), StandardCharsets.UTF_8).trim
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
