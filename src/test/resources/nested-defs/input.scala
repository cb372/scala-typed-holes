package foo

object Foo {

  def parent(x: Int, y: String): Boolean = {

    def child1(x: Int, z: Boolean): Double = {
      ???
    }

    def child2(x: Int): String = {
      if (y.length == x) {
        ???
      } else {
        val umm = "123"
        ???
      }
    }

    child1(123, true) == 0.0
  }

}
