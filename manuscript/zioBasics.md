## zioBasics

 

### experiments/src/main/scala/zioBasics/Alias.scala
```scala
package zioBasics

import zio._

object Alias:

  // suc(1-5) are all equivalant.
  // Their types can be aliased to be more and
  // more specific.
  val suc1 = ZIO.succeed(1)

  val suc2: ZIO[Any, Nothing, Int] =
    ZIO.succeed(1)
  val suc3: IO[Nothing, Int] = ZIO.succeed(1)
  val suc4: URIO[Any, Int]   = ZIO.succeed(1)
  val suc5: UIO[Int]         = ZIO.succeed(1)

```


### experiments/src/main/scala/zioBasics/Equality.scala
```scala
package zioBasics

import zio.*
import zio.Console.printLine

import java.io.IOException

val suc1                         = ZIO.succeed(1)
val suc2: ZIO[Any, Nothing, Int] = ZIO.succeed(1)
val suc3: IO[Nothing, Int]       = ZIO.succeed(1)

val suc4: UIO[Int]          = ZIO.succeed(1)
val suc4duplicate: UIO[Int] = ZIO.succeed(1)

val suc5: URIO[Any, Int] = ZIO.succeed(1)

val testEqualities: ZIO[Any, IOException, Unit] =
  for
    res1: Int <- suc1
    res2: Int <- suc2
    res3: Int <- suc3
    res4: Int <- suc4
    res5: Int <- suc5
    _ <-
      printLine(s"""
 res1: ${res1}
 res2: ${res2}
 res3: ${res3}
 res4: ${res4}
 res5: ${res5}
 """.stripMargin)
    _ <-
      printLine(
        (
          res1 == res2 && res2 == res3 &&
            res3 == res4 && res4 == res5
        ).toString
      )
    _ <-
      printLine(
        suc1 == suc2 && suc2 == suc3 &&
          suc3 == suc4 && suc4 == suc5
      )
    _ <-
      printLine((suc4 == suc4duplicate).toString)
  yield ()

object Equality extends ZIOAppDefault:
  def run = testEqualities

```


### experiments/src/main/scala/zioBasics/Fail.scala
```scala
// Fail.scala

package zioBasics

import zio._
import java.io.IOException

case class foo2()
@main
def MainFail() =
  val fail1: IO[Int, Nothing] = ZIO.fail(12)
  val fail2: IO[String, Nothing] =
    ZIO.fail("Hello")

  val bar: foo2 = foo2()
  val fail3: IO[foo2, Nothing] =
    ZIO
      .fail(bar) // ZIO that fails with an object

  val zioEx2: ZIO[Any, IOException, Unit] =
    Console.printLine("ZIO")

  // ZIO can even fail with other ZIO. Here is
  // an example of where a function can define
  // it's own fallback behavior.
  // Althought there may be better ways of
  // defining such a function, it is valid to
  // return another ZIO on fail.
  def processWithSelfDescribedFallbackBehavior(
      success: Boolean
  ): ZIO[Any, ZIO[
    Any,
    IOException,
    Unit
  ], String] =
    if (success)
      ZIO.succeed("Good job!")
    else
      ZIO.fail(zioEx2)
end MainFail

// Here is a more complex example of using ZIO
// fails in the context of an app searching for
// a person's credit score.

def getCreditScoreFromAgency1(
    successful: Boolean,
    fallbackIsSuccessful: Boolean
) =
  if (successful)
    ZIO.succeed(550)
  else if (fallbackIsSuccessful)
    ZIO.succeed(575)
  else
    ZIO.fail("Could not get their personal info")

def getCreditScoreFromAgency2(
    successful: Boolean
) = // This function checks to see if the credict score can be found from Agency 2
  if (successful)
    ZIO.succeed(557)
  else
    ZIO.fail("Internal system failure")

// Here we use the different ZIO-based
// functions to string together a coherent
// piece of logic.
val getCreditScore: ZIO[Any, IOException, Int] =
  getCreditScoreFromAgency1(false, true)
    .catchAll { case failureReason =>
      for
        _ <-
          Console.printLine(
            "First agency failure: " +
              failureReason
          )
        creditScore <-
          getCreditScoreFromAgency2(true)
            .catchAll(_ => ZIO.succeed(700))
      yield creditScore
    }

```


### experiments/src/main/scala/zioBasics/RuntimeEx.scala
```scala
package zioBasics

import java.io
import zio._
import zio.Console._
import zio.Console
import java.io.IOException
import console.FakeConsole

object RuntimeEx:
// This object's primary function is to
  // interpret/run other ZIO objects.
// In legacy code, it may be better to run
  // effects with a runtime object to preserve
  // the structure of the program.
// Using a runtime object allows for ZIO to be
  // run when ever and where ever.

  val runtime         = Runtime.default
  val exZio: UIO[Int] = ZIO.succeed(1)

  val exZio2: ZIO[Any, IOException, String] =
    for
      _    <- printLine("Input Word: ")
      word <- readLine
    yield word

  def displayWord(word: String) =
    println(s"Chosen Word: ${word}")

  @main
  def runZIO() =
    // Runtime excecutes the effects, and returns
    // their output value.
    println(
      Unsafe.unsafeCompat { implicit u =>
        runtime.unsafe.run(exZio)
      }
    )

    // Runtimes can be used in a function
    // parameter:
    displayWord(
      Unsafe.unsafeCompat { implicit u =>
        runtime
          .unsafe
          .run(
            exZio2.withConsole(FakeConsole.word)
          )
          .getOrThrow()
      }
    )
  end runZIO
end RuntimeEx

```

