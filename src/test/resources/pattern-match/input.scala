package foo

object Foo {

  def bar(x: Int): String = x match {
    case 0 => "zero"
    case 1 =>
      val dunno = "one?"
      ???
    case _ => ???
  }

}
