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
    case 1 => "one"
    case _ => ???
  }

  def hole1 = ???

  def hole2: List[String] = ???

  val hole3 = ???

  val hole4: Option[_] = ???

  var hole5 = ???

  var hole6: Option[String] = ???
  
}
