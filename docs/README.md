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

## Named holes

The plugin also supports named holes. Instead of using `???`, you can give
custom names to your holes.

For example, code like this

```scala
def hello(args: Array[String]): Option[Result] = Foo.doStuff(args) match {
  case Left(error)  => __left
  case Right(x)     => __right
}
```

will result in warnings like this

```
Found hole 'left' with type: Option[Result]
Relevant bindings include
  args: Array[String] (bound at input.scala:11:13)
  error: String (bound at input.scala:12:15)

    case Left(error)  => __left
                         ^
```

Named holes must start with a double underscore.

Warning: if you happen to use a naming convention that includes double
underscores (which is pretty rare in Scala), this plugin will probably trash
your code!

## How to use

In sbt:

```
addCompilerPlugin("com.github.cb372" % "scala-typed-holes" % "@VERSION@" cross CrossVersion.full)
```

The plugin is published for the following Scala versions:

* 2.11.12
* 2.12.{12, 13}
* 2.13.{0, 1, 2, 3, 4, 5}

## Changing the log level

By passing a compiler option `-P:typed-holes:log-level:<level>`, you can control
the severity with which holes are logged.

* `info` means holes will be logged as informational messages
* `warn` means holes will be logged as compiler warnings
* `error` means holes will be logged as compiler errors, so your program will
  fail to compile if it contains any holes.

The default behaviour is to log holes as warnings.

If you are using sbt, you can pass the option like this:

```
scalacOptions += "-P:typed-holes:log-level:info"
```
