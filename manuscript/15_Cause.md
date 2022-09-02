# Cause

Consider an Evolutionary example, where a `Cause` allows us to track 
MutationExceptions throughout a length process

Cause will track all errors that occur in an application, regardless of concurrency and parallelism

Cause allows you to aggregate multiple errors of the same type

&&/Both represents parallel failures
++/Then represents sequential failures

Cause.die will show you the line that failed, because it requires a throwable
Cause.fail will not, because it can be any arbitrary type


## Automatically attached experiments.
 These are included at the end of this
 chapter because their package in the
 experiments directory matched the name
 of this chapter. Enjoy working on the
 code with full editor capabilities :D

 

### experiments/src/main/scala/cause/CauseBasics.scala
```scala
package cause

import zio._

object CauseBasics extends App:
//    ZIO.fail(Cause.fail("Blah"))
  println(
    (
      Cause.die(Exception("1")) ++
        (Cause.fail(Exception("2a")) &&
          Cause.fail(Exception("2b"))) ++
        Cause
          .stackless(Cause.fail(Exception("3")))
    ).prettyPrint
  )

object CauseZIO extends ZIOAppDefault:

  val x: ZIO[Any, Nothing, Nothing] =
    ZIO.die(Exception("Blah"))
  def run = ZIO.die(Exception("Blah"))

```


### experiments/src/main/scala/cause/MalcomInTheMiddle.scala
```scala
package cause

import zio.{ZIO, ZIOAppDefault}

object MalcomInTheMiddle extends ZIOAppDefault:
  def run =

    def turnOnLights() = throw new BurntBulb()
    class BurntBulb() extends Exception

    def getNewBulb() = throw new WobblyShelf()
    class WobblyShelf() extends Exception

    def grabScrewDriver() =
      throw new SqueakyDrawer()
    class SqueakyDrawer() extends Exception

    def sprayWD40() = throw new EmptyCan()
    class EmptyCan() extends Exception

    def driveToStore() = throw new DeadCar()
    class DeadCar() extends Exception

    def repairCar() = throw new Nagging()
    class Nagging() extends Exception

    try
      turnOnLights()
    catch
      case burntBulb: BurntBulb =>
        try
          getNewBulb()
        catch
          case wobblyShelf: WobblyShelf =>
            try
              grabScrewDriver()
            catch
              case squeakyDrawer: SqueakyDrawer =>
                try
                  sprayWD40()
                catch
                  case emptyCan: EmptyCan =>
                    try
                      driveToStore()
                    catch
                      case deadCar: DeadCar =>
                        try
                          repairCar()
                        catch
                          case nagging: Nagging =>
                            ZIO
                              .debug(
                                "What does it look like I'm doing?!"
                              )
                              .exitCode
    end try
  end run

/** try { turnOnLights } catch { case
  * burntLightBulb => try {
  */
end MalcomInTheMiddle

```


### experiments/src/main/scala/cause/MutationTracking.scala
```scala
package cause

import zio.{Cause, IO, UIO, ZIO}
import zio.Console.*

class MutationTracking:
  enum Stage:
    case Hominini,
      Chimpanzee,
      Human

object TimelineFinally extends App:
  try
    "Everything went fine"
//    throw new Exception("Straightened Spine")
  finally
    try throw new Exception("Less Hair")
    finally
      throw new Exception("Fine Voice Control")

object Timeline extends zio.ZIOAppDefault:
  val mutation1: UIO[Nothing] =
    ZIO.die(Exception("Straightened Spine"))
  val mutation2 = ZIO.die(Exception("Less Hair"))
  val mutation3 =
    ZIO.die(Exception("Fine voice control"))

  val timeline =
    (
      mutation1
        .ensuring(mutation2)
        .ensuring(mutation3)
    )
    // .sandbox
//      .catchAll { case cause: Cause[String] =>
//        printLine(cause.defects)
//      }

//  val failable: ZIO[Any, String, Nothing] = ZIO.fail("boom")
  val timelineSandboxed
      : ZIO[Any, Cause[String], Nothing] =
    timeline.sandbox

  def run = timeline.sandbox
end Timeline

```

            