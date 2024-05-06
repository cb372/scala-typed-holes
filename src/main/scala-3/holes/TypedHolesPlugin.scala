package holes
import dotty.tools.dotc.plugins.{PluginPhase, StandardPlugin}
import scala.util.control.ControlThrowable
import dotty.tools.dotc.report

class TypedHolesPlugin extends StandardPlugin:
  val name: String = "typed-holes"
  override def description: String =
    "Treat use of ??? as a hole and give a useful warning about it"

  override def init(options: List[String]): List[PluginPhase] =
    val logLevel =
      options
        .flatMap: option =>
          if option.startsWith("log-level:") then
            option.substring("log-level:".length).toLowerCase match
              case "info"  => Some(LogLevel.Info)
              case "warn"  => Some(LogLevel.Warn)
              case "error" => Some(LogLevel.Error)
              case _       => None
          else None
        .headOption
        .getOrElse(LogLevel.Warn)

    List(
      new NamedHolesPhase,
      TypedHolesPhase(logLevel)
    )
