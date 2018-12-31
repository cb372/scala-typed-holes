object CaseApp {
  def parse[A](args: Seq[String]): Either[String, A] = Left("nope!")
}

case class Ops()

object Main {
  def main(args: Array[String]): Unit =
    println("Hello, world!")

  def parseCmdLine(args: List[String]): Option[Ops] = CaseApp.parse[Ops](args.toSeq) match {
    case Left(_)  => ???
    case Right(_) => ???
  }

  def foo:Int = ???
}