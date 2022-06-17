# Mutability

Functional programmers often sing the praises of immutability.
The advantages are real and numerous.
However, it is easy to find situations that are intrinsically mutable.

- How many people are currently inside a building?
- How much fuel is in your car?
- How much money is in your bank account?
- TODO more

It is true that many of these concepts can be derived from a sequence of state transformations.
For example, the number of people in a building can be calculated from the number of people who have entered the building and the number of people who have left the building.

```
Seq(
  Entered(2),
  Exited(1),
  Entered(3),
  Exited(2),
)
```
However, this can be tedious to work with.
We want a way to jump straight to the current state of the system.
    

```scala
import zio.{Ref, UIO, ZIO, ZIOAppDefault}
import mdoc.unsafeRunPrettyPrint
import mdoc.unsafeRunTruncate
import mdoc.wrapUnsafeZIO
import zio.Runtime.default.unsafeRun

object UnreliableMutability:
  var counter = 0
  def increment() =
    ZIO.succeed {
      counter = counter + 1
      counter
    }

  val demo: UIO[String] =
    for _ <-
        ZIO.foreachParDiscard(Range(0, 10000))(
          _ => increment()
        )
    yield "Final count: " + counter

unsafeRunPrettyPrint(UnreliableMutability.demo)
// res0: String | Unit | String = "Final count: 9995"
```

Rather than avoiding mutability entirely, we want to avoid unprincipled, unsafe mutability.
If we codify and enumerate everything that we need from Mutability, then we can wield it safely.
Required Operations:

- Change the value
- Read the current value

These are both effectful operations.
Less obviously, we also need to create the Mutable reference itself.
We are changing the world, by creating a space that we can manipulate.
A simple representation of this could look like:

```scala
trait Ref[A]:
  def get: UIO[A]
  def set(a: A): UIO[Unit]

object Ref:
  def make[A](a: A): UIO[Ref[A]] = ???
```

In order to confidently use this, we need certain guarantees about the behavior:

- The underlying value cannot be changed during a read
- Multiple writes cannot happen concurrently, which would result in lost updates


## Automatically attached experiments.
 These are included at the end of this
 chapter because their package in the
 experiments directory matched the name
 of this chapter. Enjoy working on the
 code with full editor capabilities :D

 

### experiments/src/main/scala/mutability/Refs.scala
```scala
package mutability

import mutability.UnreliableMutability.incrementCounter
import zio.{Ref, ZIO, ZIOAppDefault}

object UnreliableMutability
    extends ZIOAppDefault:
  var counter = 0
  def incrementCounter() =
    ZIO.succeed {
      counter = counter + 1
      counter
    }

  def run =
    for
      results <-
        ZIO
          .foreachParDiscard(Range(0, 10000))(
            _ => incrementCounter()
          )
          .timed
      _ <- ZIO.debug("Final count: " + counter)
      _ <-
        ZIO.debug(
          "Duration: " + results._1.toMillis
        )
    yield ()
end UnreliableMutability

object ReliableMutability extends ZIOAppDefault:
  def incrementCounter(counter: Ref[Int]) =
    counter.update(_ + 1)

  def run =
    for
      counter <- Ref.make(0)
      results <-
        ZIO
          .foreachParDiscard(Range(0, 10000))(
            _ => incrementCounter(counter)
          )
          .timed
      finalResult <- counter.get
      _ <-
        ZIO.debug("Final count: " + finalResult)
      _ <-
        ZIO.debug(
          "Duration: " + results._1.toMillis
        )
    yield ()
end ReliableMutability

object MutabilityWithComplexTypes
    extends ZIOAppDefault:

  class Sensor(lastReading: Ref[SensorData]):
    def read: ZIO[Any, Nothing, SensorData] =
      zio
        .Random
        .nextIntBounded(10)
        .map(SensorData(_))

  object Sensor:
    val make: ZIO[Any, Nothing, Sensor] =
      for lastReading <- Ref.make(SensorData(0))
      yield new Sensor(
        lastReading
      ) // TODO Why do we need new?

  case class SensorData(value: Int)

  case class World(sensors: List[Sensor])

  val arbitrarilyChangeWorldData =
    for
      sensors <-
        ZIO.foreach(List.fill(100)(0))(_ =>
          Sensor.make
        )
      world = World(sensors)
      _ <-
        ZIO
          .foreach(world.sensors)(_.read)
          .debug("Current data: ")
    yield ()

  def run = arbitrarilyChangeWorldData
//    readFromSensors

  val readFromSensors =
    for _ <- ZIO.unit
//      currentData <- Ref.make(List.fill(100)(SensorData(0)))
//      sensors = List.fill(100)(Sensor())
//      world = World(sensors, currentData)
//      _ <- world.currentData.get.debug("Current data: ")
//      updatedSensorReadings <- ZIO.foreach(world.sensors)(_.read)
//      _ <- world.currentData.set(updatedSensorReadings)
//      _ <- world.currentData.get.debug("Updated data from sensors: ")
    yield ()
end MutabilityWithComplexTypes

object Refs extends ZIOAppDefault:
  def run =
    for
      ref        <- Ref.make(1)
      startValue <- ref.get
      _ <-
        ZIO.debug("start value: " + startValue)
      _          <- ref.set(5)
      finalValue <- ref.get
      _ <-
        ZIO.debug("final value: " + finalValue)
    yield ()

```

            