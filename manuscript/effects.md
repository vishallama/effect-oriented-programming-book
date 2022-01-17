## effects

 

### randomComponents.scala
```scala
 // randomComponents.scala
package effects

import scala.util.Random

object randomComponents:

  // Anything that has a randomly generated
  // component is an effect

  def randNum: Unit =
    val rand = Random.nextInt(100)
    println(rand)

  @main
  def randNumEx =
    randNum
    randNum
// These have the same input, yet different
// outputs.

```


### CustomRandomZIO.scala
```scala
 // CustomRandomZIO.scala
package effects

import zio.{
  BuildFrom,
  Chunk,
  Console,
  Random,
  UIO,
  ZIO,
  ZLayer
}
import zio.Console.printLine

trait RandomIntBounded:
  def nextIntBounded(n: Int): UIO[Int]

object RandomIntBounded:
  object RandomIntBoundedLive
      extends RandomIntBounded:
    override def nextIntBounded(
        n: Int
    ): UIO[Int] =
      ZIO.succeed(scala.util.Random.nextInt(n))

class FakeRandomIntBounded(hardcodedValue: Int)
    extends RandomIntBounded:
  override def nextIntBounded(n: Int): UIO[Int] =
    UIO.succeed(hardcodedValue)

def luckyZ(
    i: Int
): ZIO[RandomIntBounded, Nothing, Boolean] =
  ZIO
    .environmentWithZIO[RandomIntBounded](
      _.get.nextIntBounded(i)
    )
    .map(_ == 0)

object LuckyZ extends zio.ZIOAppDefault:
  def run =
    val myRandom: ZLayer[
      Any,
      Nothing,
      RandomIntBounded
    ] = ZLayer.succeed(FakeRandomIntBounded(0))

    myAppLogic
      .provideLayer(myRandom)
      // does not work for some reason
      // .injectSome[Console](myRandom)
      .exitCode

  val myAppLogic =
    for
      isLucky <- luckyZ(50)
    yield
      if isLucky then
        "You are lucky!"
      else
        "Sorry"
end LuckyZ

trait RandomIntBetween:
  def intBetween(low: Int, high: Int): UIO[Int]

object RandomIntBetween:
  object RandomIntBetween
      extends RandomIntBetween:
    override def intBetween(
        low: Int,
        high: Int
    ): UIO[Int] =
      ZIO.succeed(
        scala.util.Random.between(low, high)
      )

class FakeRandomIntBetween(hardcodedValue: Int)
    extends RandomIntBetween:
  override def intBetween(
      low: Int,
      high: Int
  ): UIO[Int] = UIO.succeed(hardcodedValue)

def effectfulIntBetween(low: Int, high: Int) =
  ZIO.environmentWithZIO[RandomIntBetween](
    _.get.intBetween(high, low)
  )

@main
def demoStuff(): Unit =

  def foo(arg: => Int): Int =
    val x = arg
    println("In foo")
    x

  foo {
    println("hi first")
    10
  }

  def foo2(arg: () => Int): Int =
    arg()
    println("In foo2")
    arg()

  foo2 { () =>
    println("hi")
    10
  }
end demoStuff

```


### IOVars.scala
```scala
 // IOVars.scala
package effects

object IOVars {
// If the code is taking input from a non-local
  // source, or using input/output, it is
  // considered
  // an effect.

}

```


### mutableVars.scala
```scala
 // mutableVars.scala
package effects

object mutableVars:
  // Mutable variables can be considered as
  // effects. They can change the behavior of a
  // function while not being an input.

  var x = 5

  def addXnY(y: Int) = x + y

  @main
  def mutableVarsEx =
    println(addXnY(3)) // This gives 8
    x = 2
    println(addXnY(3)) // This gives 5

// The calls to addXnY have the same inputs, yet
// give different outputs. This does
// not follow the rules of a pure function.

```


### temporalVars.scala
```scala
 // temporalVars.scala
package effects

import java.util.Calendar

object temporalVars:

  // Time based functions are effectful because
  // they
  // rely on a variable that is constantly
  // changing.

  def sayTime() =
    val curTime = Calendar.getInstance()
    val curOption: Option[java.util.Calendar] =
      curTime match
        case null =>
          None
        case x: java.util.Calendar =>
          Some(x)
    val curMin =
      curOption match
        case None =>
          println("oof")
        case Some(s) =>
          s.get(Calendar.SECOND)
    println(curMin)

  @main
  def temporalVarsEx =
    sayTime()
    Thread.sleep(3000)
    sayTime()

end temporalVars

// The input for the variable is the same, yet
// there is a different output.
// The clock is thus considered an effect.

```

