package holes

import scala.collection.mutable
import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.ast.TreeDSL
import scala.tools.nsc.plugins.{Plugin, PluginComponent}

class TypedHolesPlugin(val global: Global) extends Plugin {
  val name = "typed-holes"
  val description = "Treat use of ??? as a hole and give a useful warning about it"
  val components = List(new TypedHolesComponent(this, global))
}

class TypedHolesComponent(plugin: Plugin, val global: Global)
  extends PluginComponent with TreeDSL {

  override val phaseName: String = "typed-holes"
  override val runsAfter: List[String] = List("typer")

  import global._

  override def newPhase(prev: Phase): StdPhase = new StdPhase(prev) {
    override def apply(unit: CompilationUnit) {
      new TypedHolesTraverser(unit).traverse(unit.body)
    }
  }

  class TypedHolesTraverser(unit: CompilationUnit) extends Traverser {

    case class Binding(tpe: Type, pos: Position)

    private val bindings: mutable.ArrayStack[Map[TermName, Binding]] = new mutable.ArrayStack

    override def traverse(tree: Tree): Unit = {

      tree match {
        case ValDef(_, _, tpt, Hole(holeInRhs)) =>
          warn(holeInRhs.pos, tpt.tpe)
          super.traverse(tree)
        case ValDef(_, _, _, _) =>
          super.traverse(tree)
        case DefDef(_, _, _, vparamss, tpt, Hole(holeInRhs)) =>
          bindings.push(vparamss.flatten.map(param => (param.name, Binding(param.tpt.tpe, param.pos))).toMap)
          warn(holeInRhs.pos, tpt.tpe)
          super.traverse(tree)
          bindings.pop()
        case DefDef(_, _, _, vparamss, _, _) =>
          bindings.push(vparamss.flatten.map(param => (param.name, Binding(param.tpt.tpe, param.pos))).toMap)
          super.traverse(tree)
          bindings.pop()
        case If(_, Hole(a), Hole(b)) =>
          warn(a.pos, tree.tpe)
          warn(b.pos, tree.tpe)
          super.traverse(tree)
        case If(_, Hole(a), _) =>
          warn(a.pos, tree.tpe)
          super.traverse(tree)
        case If(_, _, Hole(b)) =>
          warn(b.pos, tree.tpe)
          super.traverse(tree)
        case m @ Match(_, cases) =>
          cases foreach {
            case CaseDef(_, _, Hole(holeInBody)) =>
              warn(holeInBody.pos, m.tpe)
            case _ =>
          }
          super.traverse(tree)
        case _ =>
          super.traverse(tree)
      }

    }

    private def collectRelevantBindings: Map[Name, Binding] =
      bindings.foldLeft(Map.empty[Name, Binding]){ case (acc, level) => level ++ acc }

    private def warn(pos: Position, tpe: Type): Unit = {
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
      warning(pos, message)
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

