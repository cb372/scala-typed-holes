package foo

object Foo {
  def foo(x: Int, y: String): Boolean = true
  def bar = foo(y = ???, x = ???)
}
