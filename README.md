# scala-typed-holes

This is a Scala compiler plugin to emulate the "typed holes" feature of Haskell,
Idris, Agda, etc.

Whenever you use `???` in your code, the compiler plugin will generate a
compiler warning containing useful information about it.

For example, given the Scala code

```scala
package example

object Example {

  def foo(x: Int, y: String): Boolean = {
    if (y.length == x) {
      ??? // TODO implement!
    } else {
      true
    }
  }

  def bar(x: Int): String = x match {
    case 0 => "zero"
    case 1 => "one"
    case _ => ???
  }

}
```

you'll get warnings that look something like this:

```
[warn] /Users/chris/code/scala-typed-holes/src/test/scala/example/Example.scala:7:7:
[warn] Found hole with type: Boolean
[warn] Relevant bindings include
[warn]   x: Int (bound at Example.scala:5:11)
[warn]   y: String (bound at Example.scala:5:19)
[warn]
[warn]       ??? // TODO implement!
[warn]       ^
[warn] /Users/chris/code/scala-typed-holes/src/test/scala/example/Example.scala:16:15:
[warn] Found hole with type: String
[warn] Relevant bindings include
[warn]   x: Int (bound at Example.scala:13:11)
[warn]
[warn]     case _ => ???
[warn]               ^
```

## How to use

In sbt:

```
addCompilerPlugin("com.github.cb372" %% "scala-typed-holes" % "0.0.2")
```

## Making compilation fail

If you want to fail compilation if your program contains any holes, you can pass
the `-Xfatal-warnings` flag to the compiler.
