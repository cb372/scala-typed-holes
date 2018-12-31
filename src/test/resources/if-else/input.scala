package foo

object Foo {

  def a(x: Int, y: String): Boolean = {
    if (y.length == x) {
      ???
    } else {
      val umm = "123"
      ???
    }
  }

  def b(x: Int, y: String): Boolean =
    if (y.length == x) ???
    else true

}
