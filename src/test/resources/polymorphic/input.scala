package foo

object Foo {

  def polymorphic[A](x: A, y: A): String = {
    if (x == y) "equal"
    else ???
  }

}