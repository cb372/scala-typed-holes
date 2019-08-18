package holes

sealed abstract class LogLevel
object LogLevel {
  case object Info extends LogLevel
  case object Warn extends LogLevel
  case object Error extends LogLevel
}
