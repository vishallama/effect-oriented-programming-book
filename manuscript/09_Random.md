# Random

There 

{{Subject Dependencies: `Console`, `ZIO.serviceWith`}}

TODO All the prose to justify these hoops

NOTE Moved code to `experiments/src/main/scala/random` due to dependency on code not in Chapters


## Automatically attached experiments.
 These are included at the end of this 
 chapter because their package in the
 experiments directory matched the name
 of this chapter. Enjoy working on the
 code with full editor capabilities :D
 
 

### experiments/src/main/scala/random/Examples.scala
```scala
package random

import zio.{
  Tag,
  UIO,
  ZEnv,
  ZIO,
  ZIOAppArgs,
  ZIOAppDefault
}

import java.io.IOException
import scala.util.Random

def rollDice(): Int = Random.nextInt(6) + 1

@main
def randNumEx =
  println(rollDice())
  println(rollDice())

def fullRound(): String =
  val roll1 = rollDice()
  val roll2 = rollDice()

  (roll1, roll2) match
    case (6, 6) =>
      "Jackpot! Winner!"
    case (1, 1) =>
      "Snake eyes! Loser!"
    case (_, _) =>
      "Nothing interesting. Try again."

@main
def playUntilWinOrLoss() = println(fullRound())

def rollDiceZ()
    : ZIO[RandomBoundedInt, Nothing, Int] =
  RandomBoundedInt.nextIntBetween(1, 7)

import zio.ZIOApp
import zio.{Tag, UIO, ZEnv, ZIOAppArgs, ZLayer}

object randNumExZ extends ZIOApp:
  type Environment = RandomBoundedInt

  val layer = RandomBoundedInt.live

  val tag: Tag[RandomBoundedInt] =
    Tag[RandomBoundedInt]
  def run =
    for
      roll1 <- rollDiceZ()
      roll2 <- rollDiceZ()
      _     <- ZIO.debug(roll1)
      _     <- ZIO.debug(roll2)
    yield ()

val fullRoundZ
    : ZIO[RandomBoundedInt, Nothing, String] =
  val roll1 = rollDice()
  val roll2 = rollDice()

  for
    roll1 <- rollDiceZ()
    roll2 <- rollDiceZ()
  yield (roll1, roll2) match
    case (6, 6) =>
      "Jackpot! Winner!"
    case (1, 1) =>
      "Snake eyes! Loser!"
    case (_, _) =>
      "Nothing interesting. Try again."

// The problem above is that you can isolate the winner logic and adequately test the program. The next example cannot be split so easily.

import zio.Ref

case class GameState()

val threeChances =
  for
    remainingChancesR <- Ref.make(3)
    _ <-
      (
        for
          roll <- rollDiceZ()
          _    <- ZIO.debug(roll)
          remainingChances <-
            remainingChancesR.get
          _ <-
            remainingChancesR
              .set(remainingChances - 1)
        yield ()
      ).repeatWhileZIO(x =>
        remainingChancesR.get.map(_ > 0)
      )
  yield ()

object ThreeChances extends ZIOAppDefault:
  def run =
    threeChances.provide(RandomBoundedInt.live)

```


### experiments/src/main/scala/random/RandomBoundedInt.scala
```scala
package random

import zio.{Tag, UIO, ZEnv, ZIO, ZIOAppArgs}
import scala.util.Random

trait RandomBoundedInt:
  def nextIntBetween(
      minInclusive: Int,
      maxExclusive: Int
  ): UIO[Int]

import zio.{UIO, ZIO, ZLayer}

import scala.util.Random

object RandomBoundedInt:
  def nextIntBetween(
      minInclusive: Int,
      maxExclusive: Int
  ): ZIO[RandomBoundedInt, Nothing, Int] =
    ZIO.serviceWithZIO[RandomBoundedInt](
      _.nextIntBetween(
        minInclusive,
        maxExclusive
      )
    )

  object RandomBoundedIntLive
      extends RandomBoundedInt:
    override def nextIntBetween(
        minInclusive: Int,
        maxExclusive: Int
    ): UIO[Int] =
      ZIO.succeed(
        Random
          .between(minInclusive, maxExclusive)
      )

  val live
      : ZLayer[Any, Nothing, RandomBoundedInt] =
    ZLayer.succeed(RandomBoundedIntLive)
end RandomBoundedInt

```


### experiments/src/main/scala/random/RandomBoundedIntFake.scala
```scala
package random

import zio.{Ref, UIO, ZIO}

class RandomBoundedIntFake private (
    values: Ref[Seq[Int]]
) extends RandomBoundedInt:
  def nextIntBetween(
      minInclusive: Int,
      maxExclusive: Int
  ): UIO[Int] =
    for
      remainingValues <- values.get
      nextValue <-
        if (remainingValues.isEmpty)
          ZIO.die(
            new Exception(
              "Did not provide enough values!"
            )
          )
        else
          ZIO.succeed(remainingValues.head)
      _ <- values.set(remainingValues.tail)
    yield remainingValues.head
end RandomBoundedIntFake

object RandomBoundedIntFake:
  def apply(
      values: Seq[Int]
  ): ZIO[Any, Nothing, RandomBoundedInt] =
    for
      valuesR <- Ref.make(values)
    yield new RandomBoundedIntFake(valuesR)

```


### experiments/src/main/scala/random/RandomGuessingGame.scala
```scala
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

val effectfulGuessingGame =
  for
    _ <- Console.print(prompt)
    answer <-
      RandomBoundedInt.nextIntBetween(low, high)
    guess <- Console.readLine
    response = checkAnswer(answer, guess)
  yield prompt + guess + "\n" + response

@main
def runEffectfulGuessingGame =
  unsafeRun(
    effectfulGuessingGame.provideLayer(
      ZLayer.succeed(FakeConsole.single("3")) ++
        RandomBoundedInt.live
    )
  )

```


### experiments/src/main/scala/random/RandomZIOFake.scala
```scala
package random

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

class RandomZIOFake(i: Int) extends Random:
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

end RandomZIOFake

```

            