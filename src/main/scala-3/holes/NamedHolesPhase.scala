package holes

import dotty.tools.dotc.plugins.PluginPhase
import dotty.tools.dotc.parsing.Parser
import dotty.tools.dotc.typer.TyperPhase
import dotty.tools.dotc.core.Contexts.Context
import dotty.tools.dotc.ast.tpd
import dotty.tools.dotc.ast.untpd
import dotty.tools.dotc.core.Names.Name
import dotty.tools.dotc.util.Property
import dotty.tools.dotc.core.Names.TermName
import dotty.tools.dotc.core.StdNames
import dotty.tools.dotc.report
import dotty.tools.dotc.core.Contexts.ctx
import dotty.tools.dotc.core.Contexts.atPhase
import dotty.tools.dotc.transform.MegaPhase
import dotty.tools.dotc.ast.untpd.TreeTraverser
import dotty.tools.dotc.ast.Trees.CaseDef
import dotty.tools.dotc.ast.untpd.UntypedTreeMap

class NamedHolesPhase extends PluginPhase:
  override def phaseName: String = "named-holes"

  override val runsAfter: Set[String] = Set(Parser.name)
  override val runsBefore: Set[String] = Set(TyperPhase.name)

  override def prepareForUnit(tree: tpd.Tree)(using Context): Context =
    val namedHolesTreeMap = new UntypedTreeMap {
      override def transform(tree: tpd.Tree)(using Context): tpd.Tree =
        tree match
          case tpd.Ident(NamedHole(name)) =>
            val copied = cpy.Ident(tree)(StdNames.nme.???)
            copied.putAttachment(NamedHole.NamedHole, HoleName(name))
            copied
          case _ =>
            super.transform(tree)
    }
    ctx.compilationUnit.untpdTree =
      namedHolesTreeMap.transform(ctx.compilationUnit.untpdTree)
    ctx

object NamedHole:
  val NamedHole: Property.Key[HoleName] = Property.StickyKey()
  val pattern = "^__([a-zA-Z0-9_]+)$".r

  def unapply(name: Name): Option[String] =
    pattern.unapplySeq(name.decode.toString).flatMap(_.headOption)
