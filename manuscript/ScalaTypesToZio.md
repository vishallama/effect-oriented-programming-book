## ScalaTypesToZio

 

### FutureToZio.scala
```scala
 // FutureToZio.scala
// EitherToZio.scala
package ScalaTypesToZio

import zio._

import java.io
import java.io.IOException
import scala.concurrent.Future

class FutureToZio:

  lazy val sFuture =
    Future.successful("Success!")

```


### TryToZio.scala
```scala
 // TryToZio.scala
// TryToZio.scala
package ScalaTypesToZio

import zio._
import java.io
import java.io.IOException
import scala.util.Try

class TryToZio:
  val dividend = 42
  val divisor  = 7

  def sTry: Try[Int] = Try(dividend / divisor)

  val zTry: IO[Throwable, Int] =
    ZIO.fromTry(sTry)

```


### EitherToZio.scala
```scala
 // EitherToZio.scala
// EitherToZio.scala
package ScalaTypesToZio

import zio._

import java.io
import java.io.IOException

class EitherToZio:
  // Depending on the input, sEither can be a
  // String or an Int
  val input = "I am a string"

  val sEither: Either[String, Int] =
    try
      Right(
        input.toInt
      ) // Right case is an integer
    catch
      case e: NumberFormatException =>
        Left(input) // Left case is an error type

  // We can translate this directly into a more
  // succinct and readable IO.

  val zEither: IO[String, Int] =
    ZIO.fromEither(sEither)
end EitherToZio

```


### OptionToZio.scala
```scala
 // OptionToZio.scala
// OptionToZio.scala
package ScalaTypesToZio

import java.io
import zio._
import java.io.IOException

class OptionToZio:

  val sOption1: Option[Int] =
    Some(1) // sOption is either 1 or None

  // Here we convert the Scala Options to ZIOs
  // using .fromOption()
  val zOption1: IO[Option[Nothing], Int] =
    ZIO.fromOption(sOption1)

  case class Person(name: String)
  val person1                  = Person("Bob")
  val sOption2: Option[Person] = Some(person1)

  val zOption2: IO[Option[Nothing], Person] =
    ZIO.fromOption(sOption2)

```

