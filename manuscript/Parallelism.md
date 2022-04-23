## Parallelism

 

### experiments/src/main/scala/Parallelism/BasicFiber.scala
```scala
package Parallelism

import java.io.IOException
import zio.Console
import zio.{Fiber, IO, Runtime, UIO, ZIO, ZLayer}

object BasicFiber:

  // Fibers model a running IO: Fiber[E,A]. They
  // have an error type, and a success type.
  // They don't need an input environment type.
  // They are not technically effects, but they
  // can be converted to effects.

  object computation: // This object performs a computation that takes a long time. It is a recursive Fibonacci Sequence generator.

    def fib(n: Long): UIO[Long] =
      UIO
        .succeed {
          if (n <= 1)
            UIO.succeed(n)
          else
            fib(n - 1).zipWith(fib(n - 2))(_ + _)
        }
        .flatten

  // Fork will take an effect, and split off a
  // Fiber version of it.
  // This ZIO will output a Fiber that is
  // computing the 100th digit of the Fibonacci
  // Sequence.
  val fib100: UIO[Fiber[Nothing, Long]] =
    for fiber <- computation.fib(100).fork
    yield fiber

  // Part of the power of Fibers is that many of
  // them can be described and run at once.
  // This function uses two numbers (n and m),
  // and outputs two Fibers that will find the
  // n'th and m'th Fibonacci numbers
  val n: Long = 50
  val m: Long = 100

  val fibNandM
      : UIO[Vector[Fiber[Nothing, Long]]] =
    for
      fiberN <- computation.fib(n).fork
      fiberM <- computation.fib(m).fork
    yield Vector(fiberN, fiberM)
end BasicFiber

```


### experiments/src/main/scala/Parallelism/Compose.scala
```scala
package Parallelism

import java.io.IOException
import zio._
import zio.Console._
import zio.Fiber._

class Compose:
  // Composing Fibers will combine 2 or more
  // fibers into a single fiber. This new fiber
  // will produce the results of both. If any of
  // the fibers fail, the entire zipped fiber
  // will also fail.

  // Note: The results of the zipped fibers will
  // be put into a tuple.

  val helloGoodbye: UIO[Tuple] =
    for
      greeting <- IO.succeed("Hello!").fork
      farewell <- IO.succeed("GoodBye!").fork
      totalFiber =
        greeting.zip(
          farewell
        ) // Note the '=', not '<-'
      tuple <- totalFiber.join
    yield tuple

  // A very useful fiber method or composing is
  // the 'orElse' method.
  // This method will combine two fibers. If the
  // first succeeds, the composed fiber will
  // succeed with first fiber's result. If the
  // first fails, the second will be used.

  val isPineapple: IO[String, String] =
    IO.succeed("Pineapple!")

  val notPineapple: IO[String, String] =
    IO.fail("Banana...")

  val composeFruit: IO[String, String] =
    for
      fFiber <-
        notPineapple
          .fork // notPineapple will fail
      sFiber <-
        isPineapple
          .fork // isPineapple will succeed
      totalFiber = fFiber.orElse(sFiber)
      output <-
        totalFiber
          .join // The output effect will end up using isPineapple.
    yield output
end Compose

```


### experiments/src/main/scala/Parallelism/Finalizers.scala
```scala
package Parallelism

import java.io.IOException
import zio.Console.printLine
import zio.Console
import zio.{
  Fiber,
  IO,
  Runtime,
  UIO,
  URIO,
  ZIO,
  ZLayer
}

import scala.io.Source._

object Finalizers extends zio.ZIOAppDefault:

  // In this example, we create a ZIO that uses
  // file IO. It opens a file to read it, but
  // gets failed half way through.
  // We use a finalizer to ensure that even if
  // the ZIO fails unexpectedly, the file will
  // still be closed.

  def finalizer(
      source: scala.io.Source
  ) = // Define the finalizer behavior here
    UIO.succeed {
      println("Finalizing: Closing file reader")
      source.close // Close the input source
    }

  val readFileContents
      : ZIO[Any, Throwable, Vector[String]] =
    ZIO
      .succeed(
        scala
          .io
          .Source
          .fromFile(
            "src/main/scala/Parallelism/csvFile.csv"
          )
      ) // Open the file to read its contents
      .acquireReleaseWith(finalizer) {
        bufferedSource => // Use the bracket method with the finalizer defined above to define behavior on fail.

          val lines =
            for line <- bufferedSource.getLines
            yield line

          if (
            true
          ) // Simulating an enexpected error/exception
            throw new IOException("Boom!")

          ZIO.succeed(Vector() ++ lines)
      }

  def run = // Use App's run function
    println("In main")

    val ioExample: ZIO[
      Any,
      Throwable,
      Unit
    ] = // Define the ZIO contexts
      for
        fileLines <- readFileContents
        _ <-
          printLine(
            fileLines.mkString("\n")
          ) // Combine the strings of the output vector into a single string, separated by \n
      yield ()
    ioExample
      .catchAllDefect(exception =>
        printLine(
          "Ultimate error message: " +
            exception.getMessage
        )
      )
      .exitCode // Call the Zio with exitCode.
  end run
end Finalizers

```


### experiments/src/main/scala/Parallelism/Interrupt.scala
```scala
package Parallelism

import java.io.IOException
import zio._
import zio.Console._
import zio.durationInt

class Interrupt:
  val n = 100

  // This ZIO does nothing but count to n.
  // It is not productive, but it uses resources.
  val countToN: ZIO[Clock, Nothing, Unit] =
    for _ <- ZIO.sleep(n.seconds)
    yield ()

  // This effect will create a fiber vrsion of
  // countToN.
  // It will then interrupt the fiber, which
  // returns an exit object.
  // Note: Interrupting Fibers is completely
  // safe.
  // Interrupt safely releases all resources, and
  // runs the finalizers.
  val noCounting: ZIO[Clock, Nothing, Exit[
    Nothing,
    Unit
  ]] =
    for
      fiber <- countToN.fork
      exit  <- fiber.interrupt
    yield exit
end Interrupt

```


### experiments/src/main/scala/Parallelism/Join.scala
```scala
package Parallelism

import java.io.IOException
import zio.{Fiber, IO, Runtime, UIO, ZIO, ZLayer}

class Join:

  // Joining a fiber converts it into an effect.
  // This effect will succeed or fail depending
  // on the fiber.
  val joinedFib100
      : UIO[Long] = // This function makes a fiber, then joins the fiber, and returns it as an effect
    for
      fiber <-
        computation
          .fib(100)
          .fork // Fiber is made to find 100th value of Fib
      output <-
        fiber
          .join // Fiber is converted into an effect, then returned.
    yield output

  // This object performs a computation that
  // takes a long time. It is a recursive
  // Fibonacci Sequence generator.
  object computation:

    def fib(n: Long): UIO[Long] =
      UIO
        .succeed {
          if (n <= 1)
            UIO.succeed(n)
          else
            fib(n - 1).zipWith(fib(n - 2))(_ + _)
        }
        .flatten
end Join

```


### experiments/src/main/scala/Parallelism/JustSleep.scala
```scala
package Parallelism

import java.io.IOException
import zio.durationInt
import zio.{
  Fiber,
  IO,
  Runtime,
  UIO,
  ZIO,
  ZIOAppDefault,
  ZLayer
}

import scala.concurrent.Await

object JustSleep extends ZIOAppDefault:

  override def run =
    ZIO.collectAllPar(
      (1 to 10000).map(_ => ZIO.sleep(1.seconds))
    ) *>
      ZIO.debug(
        "Finished far sooner than 10,000 seconds"
      )

@main
def ToFuture() =
  Await.result(
    Runtime
      .default
      .unsafeRunToFuture(ZIO.sleep(1.seconds)),
    scala.concurrent.duration.Duration.Inf
  )

```

