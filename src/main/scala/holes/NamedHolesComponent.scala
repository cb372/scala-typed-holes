package holes

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.transform.Transform
import scala.tools.nsc.ast.TreeDSL
import scala.tools.nsc.plugins.{Plugin, PluginComponent}

class NamedHolesComponent(plugin: Plugin, val global: Global)
  extends PluginComponent with TreeDSL with Transform {

  override val phaseName: String = "named-holes"
  override val runsAfter: List[String] = List("parser")
  override val runsBefore: List[String] = List("namer")

  import global._

  override def newTransformer(unit: CompilationUnit): Transformer =
    new NamedHolesTransformer(unit)

  class NamedHolesTransformer(unit: CompilationUnit) extends Transformer {

    object NamedHole {
      val pattern = "^__([a-zA-Z0-9_]+)$".r

      def unapply(name: Name): Option[String] =
        pattern.unapplySeq(name.decoded.toString).flatMap(_.headOption)
    }

    override def transform(tree: Tree): Tree = {
      val t = super.transform(tree)
      t match {
        case Ident(NamedHole(name)) =>
          atPos(t.pos)(treeCopy.Ident(t, TermName("$qmark$qmark$qmark")))
            .updateAttachment(HoleName(name))
        case _ =>
          t
      }
    }

  }

}
