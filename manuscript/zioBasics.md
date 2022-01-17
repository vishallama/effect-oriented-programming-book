## zioBasics

 

### Alias.scala
```scala
 // Alias.scala
package zioBasics

import java.io
import zio._
import java.io.IOException

object Alias:
  // General Alias Table:
  // UIO[A] = ZIO[Any, Nothing, A] Any
  // environment, No Errors
  // URIO[R,A] = ZIO[R, Nothing, A]    No errors
  // Task[A] = ZIO[Any, Throwable, A] Any
  // environment, Throwable errors
  // RIO[A] = ZIO[R, Throwable, A] Throwable
  // Errors
  // IO[E,A] = ZIO[Any, E, A] Any Environment

  // suc(1-5) are all equivalant.
  // Their types can be aliased to be more and
  // more specific.
  val suc1 = ZIO.succeed(1)

  val suc2: ZIO[Any, Nothing, Int] =
    ZIO.succeed(1)
  val suc3: IO[Nothing, Int] = ZIO.succeed(1)
  val suc4: URIO[Any, Int]   = ZIO.succeed(1)
  val suc5: UIO[Int]         = ZIO.succeed(1)
end Alias

```


### Fail.scala
```scala
 // Fail.scala
// Fail.scala

package zioBasics

import zio._
import java.io
import java.io.IOException

object Fail:

  @main
  def MainFail() =
    // fail(1-3) are effects that fail with the
    // specified value
    // Any parameter type can be passed in.
    val fail1: IO[Int, Nothing] =
      ZIO.fail(12) // ZIO that fails with 12
    val fail2: IO[String, Nothing] =
      ZIO.fail(
        "Hello"
      ) // ZIO that fails with Hello

    val bar: foo2 = foo2()
    val fail3: IO[foo2, Nothing] =
      ZIO.fail(
        bar
      ) // ZIO that fails with an object

    val zioEx2: ZIO[Console, IOException, Unit] =
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
      Console,
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

  def getCreditScoreFromAgency1( // This function checks to see if the credict score can be found from Agency 1
      successful: Boolean,
      fallbackIsSuccessful: Boolean
  ) =
    if (successful)
      ZIO.succeed(550)
    else if (fallbackIsSuccessful)
      ZIO.succeed(575)
    else
      ZIO.fail(
        "Could not get their personal info"
      )

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
  val getCreditScore
      : ZIO[Console, IOException, Int] =
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
end Fail

case class foo2()

```


### Equality.scala
```scala
 // Equality.scala
package zioBasics

import java.io
import zio._
import zio.Console._

import java.io.IOException

object Equality extends zio.App:

  // Equality may be non-intuative when it comes
  // to ZIO.
  // suc(1-5) are all equivalant. They all equal
  // a success with a return of 1.
  // (Their types can be aliased to be more and
  // more specific.)
  val suc1 = ZIO.succeed(1)

  val suc2: ZIO[Any, Nothing, Int] =
    ZIO.succeed(1)
  val suc3: IO[Nothing, Int] = ZIO.succeed(1)
  val suc4: UIO[Int]         = ZIO.succeed(1)

  val suc4d: UIO[Int] =
    ZIO.succeed(1) // (Duplicate of suc4)
  val suc5: URIO[Any, Int] = ZIO.succeed(1)

  // Here, we test the equality values of all the
  // ZIO:
  val myAppLogic
      : ZIO[Console, IOException, Unit] =
    for
      res1: Int <-
        suc1 // Flat map all the ZIO into their integer values
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
      // Test if the flat mapped ZIO are
      // equivelant:
      _ <-
        printLine(
          (
            res1 == res2 && res2 == res3 &&
              res3 == res4 && res4 == res5
          ).toString
        )
      // Test if the differently aliased ZIO are
      // considered equivelant:
      _ <-
        printLine(
          (
            suc1 == suc2 && suc2 == suc3 &&
              suc3 == suc4 && suc4 == suc5
          )
        )
      // Test if identically defined ZIO are
      // considered equivelant:
      _ <- printLine((suc4 == suc4d).toString)
    yield ()

  // Until the ZIO are run, they cannot be
  // considered equivelant to other ZIO. Becuase
  // they represent a certain level of
  // uncertainty, their exact value cannot be
  // determined.

  def run(args: List[String]) =
    myAppLogic.exitCode
end Equality

```


### RuntimeEx.scala
```scala
 // RuntimeEx.scala
package zioBasics

import java.io
import zio._
import zio.Console._
import zio.Console
import java.io.IOException
import fakeEnvironmentInstances.FakeConsole

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

  val exZio2: ZIO[Console, IOException, String] =
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
    println(runtime.unsafeRun(exZio))

    // Runtimes can be used in a function
    // parameter:
    displayWord(
      runtime.unsafeRun(
        exZio2.provide(
          ZLayer.succeed(FakeConsole.word)
        )
      )
    )
end RuntimeEx

```

