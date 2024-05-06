package foo

object Foo {

  def hole1 = ???

  def hole2: List[String] = ???

  val hole3 = ???

  val hole4: Option[_] = ???

  var hole5 = ???

  var hole6: Option[String] = ???

  val hole7 = { ??? }

  val hole8: () => Int = { ??? }

  val hole9 = { (x: String) => ??? }

  val hole10: String => Int = { x => ??? }

  val hole11 = { (a: Int, b: String) => ??? }

  val hole12: (Int, String) => Int = { (a, b) => ??? }

  val hole13: (Int, String) => (String, Int) = { (a, b) => ??? }

  val hole14: String => Int = { s => s.foldLeft(0) { (a, c) => ??? } }

}