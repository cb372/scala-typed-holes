package holes

import scala.collection.mutable
import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.ast.TreeDSL
import scala.tools.nsc.plugins.{Plugin, PluginComponent}

sealed abstract class LogLevel
object LogLevel {
  case object Info extends LogLevel
  case object Warn extends LogLevel
  case object Error extends LogLevel
}

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

  val components = List(new TypedHolesComponent(this, global, () => logLevel))

}

class TypedHolesComponent(plugin: Plugin, val global: Global, getLogLevel: () => LogLevel)
  extends PluginComponent with TreeDSL {

  override val phaseName: String = "typed-holes"
  override val runsAfter: List[String] = List("typer")

  import global._

  override def newPhase(prev: Phase): StdPhase = new StdPhase(prev) {
    override def apply(unit: CompilationUnit): Unit = {
      new TypedHolesTraverser(unit).traverse(unit.body)
    }
  }

  class TypedHolesTraverser(unit: CompilationUnit) extends Traverser {

    case class Binding(tpe: Type, pos: Position)

    private val bindings: mutable.ArrayStack[Map[TermName, Binding]] = new mutable.ArrayStack

    override def traverse(tree: Tree): Unit = {

      tree match {
        case ValDef(_, _, tpt, Hole(holeInRhs)) =>
          log(holeInRhs.pos, tpt.tpe)
          super.traverse(tree)
        case ValDef(_, _, _, _) =>
          super.traverse(tree)
        case DefDef(_, _, _, vparamss, tpt, Hole(holeInRhs)) =>
          bindings.push(vparamss.flatten.map(param => (param.name, Binding(param.tpt.tpe, param.pos))).toMap)
          log(holeInRhs.pos, tpt.tpe)
          super.traverse(tree)
          bindings.pop()
        case DefDef(_, _, _, vparamss, _, _) =>
          bindings.push(vparamss.flatten.map(param => (param.name, Binding(param.tpt.tpe, param.pos))).toMap)
          super.traverse(tree)
          bindings.pop()
        case If(_, Hole(a), Hole(b)) =>
          log(a.pos, tree.tpe)
          log(b.pos, tree.tpe)
          super.traverse(tree)
        case If(_, Hole(a), _) =>
          log(a.pos, tree.tpe)
          super.traverse(tree)
        case If(_, _, Hole(b)) =>
          log(b.pos, tree.tpe)
          super.traverse(tree)
        case m @ Match(_, cases) =>
          cases foreach {
            case CaseDef(_, _, Hole(holeInBody)) =>
              log(holeInBody.pos, m.tpe)
            case _ =>
          }
          super.traverse(tree)
        case _ =>
          super.traverse(tree)
      }

    }

    private def collectRelevantBindings: Map[TermName, Binding] =
      bindings.foldLeft(Map.empty[TermName, Binding]){ case (acc, level) => level ++ acc }

    private def log(pos: Position, tpe: Type): Unit = {
      val relevantBindingsMessages =
        collectRelevantBindings.map {
          case (boundName, Binding(boundType, bindingPos)) => s"  $boundName: $boundType (bound at ${posSummary(bindingPos)})"
        }
          .mkString("\n")
      val message =
        if (!relevantBindingsMessages.isEmpty)
          s"""
             |Found hole with type: $tpe
             |Relevant bindings include
             |$relevantBindingsMessages
           """.stripMargin
        else
          s"Found hole with type: $tpe"
      getLogLevel() match {
        case LogLevel.Info => inform(pos, message)
        case LogLevel.Warn => warning(pos, message)
        case LogLevel.Error => globalError(pos, message)
      }
    }

    private def posSummary(pos: Position): String =
      s"${pos.source.file.name}:${pos.line}:${pos.column}"

    object Hole {
      def unapply(tree: Tree): Option[Tree] = tree match {
        case _ if tree.symbol == definitions.Predef_??? =>
          Some(tree)
        case Block(_, expr) if expr.symbol == definitions.Predef_??? =>
          Some(expr)
        case _ =>
          None
      }
    }

  }


}

