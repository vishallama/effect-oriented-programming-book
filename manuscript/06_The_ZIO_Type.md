# The ZIO Type

The `ZIO` trait is at the center of our Effect-oriented world.

```scala
???
```

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

import zio._

import java.io
import java.io.IOException

case class InvalidIntegerInput(value: String)

def parseInteger(
    input: String
): Either[InvalidIntegerInput, Int] =
  try
    Right(
      input.toInt
    ) // Right case is an integer
  catch
    case e: NumberFormatException =>
      Left(
        InvalidIntegerInput(input)
      ) // Left case is an error type
object EitherToZio extends ZIOAppDefault:

  val zEither: IO[InvalidIntegerInput, Int] =
    ZIO.fromEither(parseInteger("42"))

  def run = zEither

```


### experiments/src/main/scala/the_zio_type/FutureToZio.scala
```scala
package the_zio_type

import zio._

import java.io
import java.io.IOException
import scala.concurrent.Future

object FutureToZio extends ZIOAppDefault:

  lazy val sFuture: Future[String] =
    Future.successful("Success!")
  // Future.failed(new Exception("Failure :("))

  val run =
    ZIO.fromFuture(implicit ec => sFuture)

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

            