package a

object A:
  opaque type OpaqueInt = Int
  given OpaqueInt = 1

object O:
  def f(i: Int, s: String) = i
  f(???, if(1 == 2) ??? else ???)

  def ff(i: Int)(s: String)(u: O.type) = 1
  ff(1)(???){ ??? }

  import A.*
  def m(j: OpaqueInt ?=> Int): Int = j
  m(???)

