package holes

import scala.collection.mutable
import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.ast.TreeDSL
import scala.tools.nsc.plugins.{Plugin, PluginComponent}

class TypedHolesComponent(plugin: Plugin, val global: Global, getLogLevel: () => LogLevel)
  extends PluginComponent with TreeDSL {

  override val phaseName: String = "typed-holes"
  override val runsAfter: List[String] = List("typer")
  override val runsBefore: List[String] = List("patmat")

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
          log(holeInRhs, tpt.tpe)
          super.traverse(tree)
        case ValDef(_, _, tpt, Function(vparams, Hole(body))) =>
          bindings.push(vparams.map(param => (param.name, Binding(param.tpt.tpe, param.pos))).toMap)
          log(body, tpt.tpe.typeArgs.last)
          super.traverse(tree)
          bindings.pop()
        case ValDef(_, _, _, _) =>
          super.traverse(tree)
        case DefDef(_, _, _, vparamss, tpt, Hole(holeInRhs)) =>
          bindings.push(vparamss.flatten.map(param => (param.name, Binding(param.tpt.tpe, param.pos))).toMap)
          log(holeInRhs, tpt.tpe)
          super.traverse(tree)
          bindings.pop()
        case DefDef(_, _, _, vparamss, _, _) =>
          bindings.push(vparamss.flatten.map(param => (param.name, Binding(param.tpt.tpe, param.pos))).toMap)
          super.traverse(tree)
          bindings.pop()
        case Function(vparams, _) =>
          bindings.push(vparams.map(param => (param.name, Binding(param.tpt.tpe, param.pos))).toMap)
          super.traverse(tree)
          bindings.pop()
        case If(_, Hole(a), Hole(b)) =>
          log(a, tree.tpe)
          log(b, tree.tpe)
          super.traverse(tree)
        case If(_, Hole(a), _) =>
          log(a, tree.tpe)
          super.traverse(tree)
        case If(_, _, Hole(b)) =>
          log(b, tree.tpe)
          super.traverse(tree)
        case m @ Match(_, cases) =>
          cases foreach {
            case CaseDef(pat, _, Hole(holeInBody)) =>
              bindings.push(gatherPatternBindings(pat))
              log(holeInBody, m.tpe)
              bindings.pop()
            case _ =>
          }
          super.traverse(tree)
        case a @ Apply(_, args) =>
          args foreach {
            case Function(vparams, Hole(body)) =>
              bindings.push(vparams.map(param => (param.name, Binding(param.tpt.tpe, param.pos))).toMap)
              log(body, a.tpe)
              bindings.pop()
            case _ =>
          }
          super.traverse(tree)
        case _ =>
          super.traverse(tree)
      }

    }

    private def gatherPatternBindings(tree: Tree): Map[TermName, Binding] = tree match {
      case Bind(name, body) =>
        Map(name.toTermName -> Binding(body.tpe, tree.pos)) ++ gatherPatternBindings(body)
      case Apply(_, args) =>
        val bindingss = args.map { arg =>
          val namedArgBinding =
            if (arg.symbol != NoSymbol)
              Map(arg.symbol.name.toTermName -> Binding(arg.tpe, arg.pos))
            else
              Map.empty[TermName, Binding]

          val bindingsInsideArg = gatherPatternBindings(arg)

          namedArgBinding ++ bindingsInsideArg
        }
        bindingss.foldLeft(Map.empty[TermName, Binding])(_ ++ _)
      case _ =>
        Map.empty
    }

    private def collectRelevantBindings: Map[TermName, Binding] =
      bindings.foldLeft(Map.empty[TermName, Binding]){ case (acc, level) => level ++ acc }

    private def log(holeTree: Tree, tpe: Type): Unit = {
      val relevantBindingsMessages =
        collectRelevantBindings.toList.sortBy(_._1.toString).map {
          case (boundName, Binding(boundType, bindingPos)) => s"  $boundName: $boundType (bound at ${posSummary(bindingPos)})"
        }
          .mkString("\n")
      val holeName = holeTree.attachments.get[HoleName]
      val holeNameMsg = holeName.fold("")(x => s"'${x.name}' ")
      val message =
        if (!relevantBindingsMessages.isEmpty)
          s"""
             |Found hole ${holeNameMsg}with type: $tpe
             |Relevant bindings include
             |$relevantBindingsMessages
           """.stripMargin
        else
          s"Found hole with type: $tpe"
      getLogLevel() match {
        case LogLevel.Info => inform(holeTree.pos, message)
        case LogLevel.Warn => warning(holeTree.pos, message)
        case LogLevel.Error => globalError(holeTree.pos, message)
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

