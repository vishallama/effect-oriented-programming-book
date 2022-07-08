# The ZIO Type


We need an `Answer` about this scenario.  The scenario requires things and could produce an error.
```
trait ZIO[Requirements, Error, Answer]
```

One downside of these type parameters 


The `ZIO` trait is at the center of our Effect-oriented world.

```scala
trait ZIO[R, E, A]
```

```scala
import zio.ZIO
```

A trait with 3 type parameters can be intimidating, but each one serves a distinct, important purpose.

## R - The Environment

This is the piece that distinguishes the ZIO monad.
It indicates which pieces of the world we will be observing or changing.

```scala
import zio.Console

def print(
    msg: String
): ZIO[Console, Nothing, Unit] = ???
```

This type signature tells us that `print` needs a `Console` in its environment to execute.

## E - The Error

This parameter tells us how this operation might fail.

```scala
def parse(
    contents: String
): ZIO[Any, IllegalArgumentException, Unit] = ???
```

## A - The Result

This is what our code will return if it completes successfully.

```scala
def defaultGreeting()
    : ZIO[Any, Nothing, String] = ???
```

## Conversions from standard Scala types
ZIO provides simple interop with may of the built-in Scala data types, namely

- `Option`
- `Either`
- `Try`
- `scala.concurrent.Future`
- `Promise`

And even some Java types -

- `java.util.concurrent.Future`
- `AutoCloseable`

```scala
import zio.{ZIO, ZIOAppDefault}
import scala.concurrent.Future
import mdoc.unsafeRunPrettyPrint
val zFuture =
  ZIO.fromFuture(implicit ec =>
    Future.successful("Success!")
  )
// zFuture: ZIO[Any, Throwable, String] = Stateful(
//   trace = "repl.MdocSession$.App.zFuture.macro(06_The_ZIO_Type.md:47)",
//   onState = zio.ZIO$$$Lambda$13938/1655700539@5aa6c17e
// )
val zFutureFailed =
  ZIO.fromFuture(implicit ec =>
    Future.failed(new Exception("Failure :("))
  )
// zFutureFailed: ZIO[Any, Throwable, Nothing] = Stateful(
//   trace = "repl.MdocSession$.App.zFutureFailed.macro(06_The_ZIO_Type.md:54)",
//   onState = zio.ZIO$$$Lambda$13938/1655700539@2ffab6e4
// )
unsafeRunPrettyPrint(zFuture)
// res0: String = "Success!"
unsafeRunPrettyPrint(zFutureFailed)
// Should handle errors
// res1: String = "java.lang.Exception: Failure :("
```

## Automatically attached experiments.
 These are included at the end of this
 chapter because their package in the
 experiments directory matched the name
 of this chapter. Enjoy working on the
 code with full editor capabilities :D

 

### experiments/src/main/scala/the_zio_type/EitherToZio.scala
```scala
// EitherToZio.scala
package the_zio_type

import zio.{ZIO, ZIOAppDefault}

import scala.util.{Left, Right}

case class InvalidIntegerInput(value: String)

object EitherToZio extends ZIOAppDefault:
  val goodInt: Either[InvalidIntegerInput, Int] =
    Right(42)

  val zEither
      : ZIO[Any, InvalidIntegerInput, Int] =
    ZIO.fromEither(goodInt)

  def run = zEither.debug("Converted Either")

```


### experiments/src/main/scala/the_zio_type/FutureToZio.scala
```scala
package the_zio_type

import zio.{ZIO, ZIOAppDefault}
import scala.concurrent.Future

object FutureToZio extends ZIOAppDefault:

  val zFuture =
    ZIO.fromFuture(implicit ec =>
      Future.successful("Success!")
    )

  val zFutureFailed =
    ZIO.fromFuture(implicit ec =>
      Future.failed(new Exception("Failure :("))
    )

  val run =
    zFutureFailed.debug("Converted Future")

```


### experiments/src/main/scala/the_zio_type/OptionToZio.scala
```scala
package the_zio_type

import java.io
import zio._
import java.io.IOException

class OptionToZio extends ZIOAppDefault:

  val alias: Option[String] =
    Some("Buddy") // sOption is either 1 or None

  val aliasZ: IO[Option[Nothing], String] =
    ZIO.fromOption(alias)

  val run = aliasZ

```


### experiments/src/main/scala/the_zio_type/TryToZio.scala
```scala
package the_zio_type

import zio._
import java.io
import java.io.IOException
import scala.util.Try

object TryToZio extends ZIOAppDefault:
  val dividend = 42
  val divisor  = 7

  // Significant Note: Try is a standard
  // collection by-name function. This makes
  // it a good candidate for introducting that
  // concept.
  def sTry: Try[Int] = Try(dividend / divisor)

  val zTry: IO[Throwable, Int] =
    ZIO.fromTry(sTry)

  val run = zTry

```

            