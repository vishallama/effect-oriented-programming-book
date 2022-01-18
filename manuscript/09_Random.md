# Random

{{Subject Dependencies: `Console`, `ZIO.serviceWith`}}

TODO All the prose to justify these hoops

NOTE Moved code to `experiments/src/main/scala/random` due to dependency on code not in Chapters


## Automatically attached experiments.
 These are included at the end of this 
 chapter because their package in the
 experiments directory matched the name
 of this chapter. Enjoy working on the
 code with full editor capabilities :D
 
 

### FakeRandom.scala
```scala
 // FakeRandom.scala
package fakeEnvironmentInstances

import zio.{
  BuildFrom,
  Chunk,
  Console,
  Random,
  UIO,
  ZIO,
  ZLayer,
  ZTraceElement
}
import zio.Console.printLine

import java.util.UUID

trait RandomInt:
  def nextIntBounded(n: Int): UIO[Int]
  def nextInt: UIO[Int]
  def nextIntBetween(
      minInclusive: Int,
      maxExclusive: Int
  ): UIO[Int]

class FakeRandomInt(hardcodedValue: Int)
    extends RandomInt:
  override def nextIntBounded(n: Int): UIO[Int] =
    UIO.succeed(hardcodedValue)

  override def nextInt: UIO[Int] =
    UIO.succeed(hardcodedValue)
  override def nextIntBetween(
      minInclusive: Int,
      maxExclusive: Int
  ): UIO[Int] = UIO.succeed(hardcodedValue)

object RandomInt:
  object RandomIntLive extends RandomInt:
    // Consider whether to re-implement from
    // scratch
    def nextIntBounded(n: Int): UIO[Int] =
      Random.RandomLive.nextIntBounded(n)

    def nextInt: UIO[Int] =
      Random.RandomLive.nextInt
    def nextIntBetween(
        minInclusive: Int,
        maxExclusive: Int
    ): UIO[Int] =
      Random
        .RandomLive
        .nextIntBetween(
          minInclusive,
          maxExclusive
        )

  val live: ZLayer[Any, Nothing, RandomInt] =
    ZLayer.succeed(RandomIntLive)
end RandomInt

class FakeRandom(i: Int) extends Random:
  def nextUUID(implicit
      trace: ZTraceElement
  ): UIO[UUID] = ???
  def nextBoolean(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Boolean] = ???
  def nextBytes(length: => Int)(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[zio.Chunk[Byte]] = ???
  def nextDouble(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Double] = ???
  def nextDoubleBetween(
      minInclusive: => Double,
      maxExclusive: => Double
  )(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Double] = ???
  def nextFloat(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Float] = ???
  def nextFloatBetween(
      minInclusive: => Float,
      maxExclusive: => Float
  )(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Float] = ???
  def nextGaussian(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Double] = ???
  def nextInt(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Int] = ???
  def nextIntBetween(
      minInclusive: => Int,
      maxExclusive: => Int
  )(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Int] = ???
  def nextIntBounded(n: => Int)(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Int] = ???
  def nextLong(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Long] = ???
  def nextLongBetween(
      minInclusive: => Long,
      maxExclusive: => Long
  )(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Long] = ???
  def nextLongBounded(n: => Long)(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Long] = ???
  def nextPrintableChar(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Char] = ???
  def nextString(length: => Int)(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[String] = ???
  def setSeed(seed: => Long)(implicit
      trace: zio.ZTraceElement
  ): zio.UIO[Unit] = ???
  def shuffle[A, Collection[+Element]
    <: Iterable[Element]](
      collection: => Collection[A]
  )(implicit
      bf: BuildFrom[Collection[A], A, Collection[
        A
      ]],
      trace: ZTraceElement
  ): UIO[Collection[A]] = ???

end FakeRandom

```


### Guess.scala
```scala
 // Guess.scala
package random

import zio.{Console, UIO, ZIO, ZLayer}
import zio.Runtime.default.unsafeRun
import console.FakeConsole

val low  = 1
val high = 10

val prompt =
  s"Pick a number between $low and $high: "

// TODO Determine how to handle .toInt failure
// possibility
def checkAnswer(
    answer: Int,
    guess: String
): String =
  if answer == guess.toInt then
    "You got it!"
  else
    s"BZZ Wrong!! Answer was $answer"

val sideEffectingGuessingGame =
  for
    _ <- Console.print(prompt)
    answer = scala.util.Random.between(low, high)
    guess <- Console.readLine
    response = checkAnswer(answer, guess)
  yield prompt + guess + "\n" + response

@main
def runSideEffectingGuessingGame =
  unsafeRun(
    sideEffectingGuessingGame.provide(
      ZLayer.succeed(FakeConsole.single("3"))
    )
  )

import zio.Console.printLine

trait RandomInt:
  def between(high: Int, low: Int): UIO[Int]

object RandomInt:
  def between(
      low: Int,
      high: Int
  ): ZIO[RandomInt, Nothing, Int] =
    // TODO Study and determine how/when to
    // introduct `serviceWith`
    ZIO
      .service[RandomInt]
      .flatMap(_.between(high, low))

  object LiveRandomIntBetween extends RandomInt:

    override def between(
        high: Int,
        low: Int
    ): UIO[Int] =
      ZIO.succeed(
        scala.util.Random.between(low, high)
      )
end RandomInt

class FakeRandomInt(hardcodedValue: Int)
    extends RandomInt:

  override def between(
      high: Int,
      low: Int
  ): UIO[Int] = UIO.succeed(hardcodedValue)

val effectfulGuessingGame =
  for
    _      <- Console.print(prompt)
    answer <- RandomInt.between(low, high)
    guess  <- Console.readLine
    response = checkAnswer(answer, guess)
  yield prompt + guess + "\n" + response

@main
def runEffectfulGuessingGame =
  unsafeRun(
    effectfulGuessingGame.provide(
      ZLayer.succeed(FakeConsole.single("3")) ++
        ZLayer
          .succeed[RandomInt](FakeRandomInt(3))
    )
  )

```

            