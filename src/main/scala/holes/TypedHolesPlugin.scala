package holes

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

    override def traverse(tree: Tree): Unit = {

      tree match {
        case ValDef(_, _, tpt, Hole(rhs)) =>
          warn(rhs.pos, tpt.tpe)
        case DefDef(_, _, _, _, tpt, Hole(rhs)) =>
          warn(rhs.pos, tpt.tpe)
        case If(_, Hole(a), Hole(b)) =>
          warn(a.pos, tree.tpe)
          warn(b.pos, tree.tpe)
        case If(_, Hole(a), _) =>
          warn(a.pos, tree.tpe)
        case If(_, _, Hole(b)) =>
          warn(b.pos, tree.tpe)
        case m@Match(_, cases) =>
          cases foreach {
            case CaseDef(_, _, body) if body.symbol == definitions.Predef_??? =>
              warn(body.pos, m.tpe)
            case _ =>
          }
        case _ =>
      }

      super.traverse(tree)
    }

    def warn(pos: Position, tpe: Type): Unit =
      warning(pos, s"Found hole with type $tpe")

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

