package holes

import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.core.Contexts.ctx
import dotty.tools.dotc.core.Names.TermName
import dotty.tools.dotc.core.Symbols.defn
import dotty.tools.dotc.core.Types.Type
import dotty.tools.dotc.interfaces.SourcePosition
import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.printing.RefinedPrinter
import dotty.tools.dotc.report
import dotty.tools.dotc.reporting.Diagnostic.Info
import dotty.tools.dotc.transform.PostTyper
import dotty.tools.dotc.typer.TyperPhase

import scala.collection.mutable
import dotty.tools.dotc.core.Flags
import dotty.tools.dotc.ast.Trees.Ident
import dotty.tools.dotc.core.StdNames.nme
import dotty.tools.dotc.core.Names.Name

class TypedHolesPhase(logLevel: LogLevel) extends PluginPhase:
  override def phaseName: String = "typed-holes"
  override val runsAfter: Set[String] = Set(TyperPhase.name)
  override val runsBefore: Set[String] = Set(PostTyper.name)

  private val bindings: mutable.Stack[Map[TermName, Binding]] =
    mutable.Stack.empty

  private val syntheticVals: mutable.Set[Name] = mutable.Set.empty

  private val defaultWidth = 1000

  override def prepareForDefDef(tree: tpd.DefDef)(using Context): Context =
    bindings.push(
      tree.termParamss.flatten
        .map(param => (param.name, Binding(param.tpt.tpe, param.sourcePos)))
        .toMap
    )
    ctx

  override def transformValDef(tree: tpd.ValDef)(using Context): tpd.Tree =
    tree match
      case tpd.ValDef(name, _, Ident(nme.???))
          if tree.symbol.is(Flags.Synthetic) =>
        syntheticVals += name
      case _ => logHole(tree.rhs, tree.tpt.tpe)
    tree

  override def transformDefDef(tree: tpd.DefDef)(using Context): tpd.Tree =
    logHole(tree.rhs, tree.tpt.tpe)
    bindings.pop()
    tree

  override def transformApply(tree: tpd.Apply)(using Context): tpd.Tree =
    val tpd.Apply(fun, args) = tree
    val index = paramIndex(fun)
    args
      .zip(fun.symbol.paramSymss(index))
      .foreach:
        case (arg, param) => logHole(arg, param.info)
    tree

  override def transformTyped(tree: tpd.Typed)(using Context): tpd.Tree =
    logHole(tree.expr, tree.tpt.tpe)
    tree

  private def paramIndex(fun: tpd.Tree, index: Int = 0)(using Context): Int =
    fun match
      case tpd.Apply(f, _) if f.symbol == fun.symbol => paramIndex(f, index + 1)
      case _                                         => index

  private def logHole(holeTree: tpd.Tree, tpe: => Type)(using Context): Unit =
    holeTree match
      case Hole(holeInRhs) => log(holeInRhs, tpe.widen)
      case Ident(name) if syntheticVals(name) =>
        syntheticVals -= name
        log(holeTree, tpe.widen)
      case tpd.Block(_, rhs) => logHole(rhs, tpe)
      case tpd.If(_, thenp, elsep) =>
        logHole(thenp, tpe)
        logHole(elsep, tpe)
      case tpd.Match(_, caseDefs)  => caseDefs.foreach(logHole(_, tpe))
      case tpd.CaseDef(_, _, tree) => logHole(tree, tpe)
      case tpd.NamedArg(_, arg)    => logHole(arg, tpe)
      case _                       =>

  private def collectRelevantBindings: Map[TermName, Binding] =
    bindings.foldLeft(Map.empty[TermName, Binding]) { case (acc, level) =>
      level ++ acc
    }

  private def log(holeTree: tpd.Tree, tpe: Type)(using Context): Unit = {
    val printer = RefinedPrinter(ctx)
    def printType(tpe: Type) =
      printer.toText(tpe).mkString(defaultWidth, false)

    val relevantBindingsMessages =
      collectRelevantBindings.toList
        .sortBy(_._1.toString)
        .map:
          case (boundName, Binding(boundType, bindingPos)) =>
            s"  $boundName: ${printType(boundType)} (bound at ${posSummary(bindingPos)})"
        .mkString("\n")

    val holeName = holeTree.getAttachment(NamedHole.NamedHole)
    val holeNameMsg = holeName.fold("")(x => s"'${x.name}' ")
    val message =
      if (!relevantBindingsMessages.isEmpty)
        s"""
           |Found hole ${holeNameMsg}with type: ${printType(tpe)}
           |Relevant bindings include
           |$relevantBindingsMessages
         """.stripMargin
      else
        s"Found hole ${holeNameMsg}with type: ${printType(tpe)}"
    logLevel match
      case LogLevel.Info =>
        ctx.reporter.report(new Info(message, holeTree.sourcePos))
      case LogLevel.Warn  => report.warning(message, holeTree.sourcePos)
      case LogLevel.Error => report.error(message, holeTree.sourcePos)
  }

  private def posSummary(pos: SourcePosition)(using Context): String =
    s"${pos.source().name()}:${pos.line + 1}:${pos.column}"

object Hole:
  def unapply(tree: tpd.Tree)(using Context): Option[tpd.Tree] =
    Option.when(tree.symbol == defn.Predef_undefined)(tree)

case class Binding(tpe: Type, pos: SourcePosition)
