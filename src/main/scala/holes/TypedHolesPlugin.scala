package holes

import scala.collection.mutable
import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.transform.Transform
import scala.tools.nsc.ast.TreeDSL
import scala.tools.nsc.plugins.{Plugin, PluginComponent}

class TypedHolesPlugin(val global: Global) extends Plugin {
  val name = "typed-holes"
  val description = "Treat use of ??? as a hole and give a useful warning about it"

  private var logLevel: LogLevel = LogLevel.Warn

  override def processOptions(options: List[String], error: String => Unit): Unit = {
    for (option <- options) {
      if (option.startsWith("log-level:")) {
        option.substring("log-level:".length).toLowerCase match {
          case "info" =>
            logLevel = LogLevel.Info
          case "warn" =>
            logLevel = LogLevel.Warn
          case "error" =>
            logLevel = LogLevel.Error
          case other =>
            error(s"Unexpected log level value: '$other'")
        }
      } else {
        error(s"Unrecognised option: $option")
      }
    }
  }

  val components = List(
    new NamedHolesComponent(this, global),
    new TypedHolesComponent(this, global, () => logLevel)
  )

}
