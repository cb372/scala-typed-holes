case class Result()

object Foo {

  def doStuff(args: Array[String]): Either[String, Int] = Left("nope!")

}

object Bar {

  def hello(args: Array[String]): Option[Result] = Foo.doStuff(args) match {
    case Left(error)  => __left
    case Right(x)     => __right
  }

}
