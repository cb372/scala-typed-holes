object CaseApp {
  def parse[A](args: Seq[String]): Either[String, A] = Left("nope!")
}

case class Ops()

object Main {

  def parseCmdLine(args: List[String]): Option[Ops] = CaseApp.parse[Ops](args.toSeq) match {
    case Left(l)  => __left
    case Right(r) => __right
  }

}
