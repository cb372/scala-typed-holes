package example

object Example {

  def foo(x: Int, y: String): Boolean = {
    if (y.length == x) {
      ???
    } else {
      val umm = "123"
      ???
    }
  }

  def bar(x: Int): String = x match {
    case 0 => "zero"
    case 1 =>
      val dunno = "one?"
      ???
    case _ => ???
  }

  def polymorphic[A](x: A, y: A): String = {
    if (x == y) "equal"
    else ???
  }

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

  def hole1 = ???

  def hole2: List[String] = ???

  val hole3 = ???

  val hole4: Option[_] = ???

  var hole5 = ???

  var hole6: Option[String] = ???

}
