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
addCompilerPlugin("com.github.cb372" % "scala-typed-holes" % "0.0.10-SNAPSHOT" cross CrossVersion.full)
```

The plugin is published for the following Scala versions:

* 2.11.12
* 2.12.1 to 2.12.9
* 2.13.0

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

