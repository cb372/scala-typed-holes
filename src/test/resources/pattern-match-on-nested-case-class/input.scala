package foo

case class Child(x: Int, y: String, z: Boolean)
case class Parent(a: String, b: Int, c: Child, d: Child, e: Child)

object Foo {

  def bar(parent: Parent): Boolean = parent match {
    case p @ Parent(wow, yeah, c @ Child(hmm, ok, _), d, Child(_, _, _)) => ???
  }
}