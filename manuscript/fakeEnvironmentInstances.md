## fakeEnvironmentInstances

 

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
/* override def nextIntBounded( n: => Int ):
 * UIO[Int] = UIO.succeed(i) override def
 * nextBoolean: UIO[Boolean] = ???
 * override def nextBytes( length: => Int ):
 * UIO[Chunk[Byte]] = ???
 * override def nextDouble: UIO[Double] = ???
 * override def nextDoubleBetween( minInclusive:
 * => Double, maxExclusive: => Double ):
 * UIO[Double] = ???
 * override def nextFloat: UIO[Float] = ???
 * override def nextFloatBetween( minInclusive:
 * => Float, maxExclusive: => Float ): UIO[Float]
 * = ???
 * override def nextGaussian: UIO[Double] = ???
 * override def nextInt: UIO[Int] = ???
 * override def nextIntBetween( minInclusive: =>
 * Int, maxExclusive: => Int ): UIO[Int] = ???
 * override def nextLong: UIO[Long] = ???
 * override def nextLongBetween( minInclusive: =>
 * Long, maxExclusive: => Long ): UIO[Long] = ???
 * override def nextLongBounded( n: => Long ):
 * UIO[Long] = ???
 * override def nextPrintableChar: UIO[Char] =
 * ???
 * override def nextString( length: => Int ):
 * UIO[String] = ???
 * override def setSeed( seed: => Long ):
 * UIO[Unit] = ???
 * def shuffle[A, Collection[+Element] <:
 * Iterable[Element]]( collection: =>
 * Collection[A] )(implicit bf:
 * BuildFrom[Collection[A], A, Collection[ A ]]
 * ): UIO[Collection[A]] = ??? */
end FakeRandom

```


### FakeConsole.scala
```scala
 // FakeConsole.scala
package fakeEnvironmentInstances

import zio._
import zio.Console
import zio.Console._

import java.io.IOException

object FakeConsole:

  val name: Console = single("(default name)")

  val word: Console   = single("Banana")
  val number: Console = single("1")

  def single(hardcodedInput: String) =
    new Console:
      def print(line: => Any)(implicit
          trace: zio.ZTraceElement
      ): zio.IO[java.io.IOException, Unit] =
        ZIO.succeed(print("Hard-coded: " + line))
      def printError(line: => Any)(implicit
          trace: zio.ZTraceElement
      ): zio.IO[java.io.IOException, Unit] = ???
      def printLine(line: => Any)(implicit
          trace: zio.ZTraceElement
      ): zio.IO[java.io.IOException, Unit] =
        ZIO.succeed(
          println("Hard-coded: " + line)
        )
      def printLineError(line: => Any)(implicit
          trace: zio.ZTraceElement
      ): zio.IO[java.io.IOException, Unit] = ???
      def readLine(implicit
          trace: zio.ZTraceElement
      ): zio.IO[java.io.IOException, String] =
        ZIO.succeed(hardcodedInput)

  def withInput(
      hardcodedInput: String*
  ): ZIO[Any, Nothing, Console] =
    for
      inputVariable <-
        Ref.make(hardcodedInput.toSeq)
    yield inputConsole(inputVariable)

  private def inputConsole(
      hardcodedInput: Ref[Seq[String]]
  ) =
    new Console:
      def print(line: => Any)(implicit
          trace: zio.ZTraceElement
      ): zio.IO[java.io.IOException, Unit] =
        IO.succeed(print(line))

      def printError(line: => Any)(implicit
          trace: zio.ZTraceElement
      ): zio.IO[java.io.IOException, Unit] = ???

      def printLine(line: => Any)(implicit
          trace: zio.ZTraceElement
      ): zio.IO[java.io.IOException, Unit] =
        ZIO
          .succeed(println("Automated: " + line))

      def printLineError(line: => Any)(implicit
          trace: zio.ZTraceElement
      ): zio.IO[java.io.IOException, Unit] = ???

      def readLine(implicit
          trace: zio.ZTraceElement
      ): zio.IO[java.io.IOException, String] =
        for
          curInput <- hardcodedInput.get
          _ <- hardcodedInput.set(curInput.tail)
        yield curInput.head

end FakeConsole

```

