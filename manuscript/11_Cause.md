# Cause

Consider an Evolutionary example, where a `Cause` allows us to track MutationExceptions throughout a length process


## Automatically attached experiments.
 These are included at the end of this 
 chapter because their package in the
 experiments directory matched the name
 of this chapter. Enjoy working on the
 code with full editor capabilities :D
 
 

### MutationTracking.scala
```scala
 // MutationTracking.scala
package cause

import zio.{ZEnv, ZIO}
import zio.Console._
import zio.Cause

class MutationTracking:
  enum Stage:
    case Hominini, Chimpanzee, Human

object Timeline extends zio.ZIOAppDefault:
  val mutation1 = ZIO.fail("Straightened Spine")
  val mutation2 =
    ZIO
      .fail("Less Hair")
      .orDieWith(new Exception(_))
  val mutation3 =
    ZIO
      .fail("Fine voice control")
      .orDieWith(new Exception(_))

  val timeline =
    mutation1
      .ensuring(mutation2)
      .ensuring(mutation3)
      .sandbox
      .catchAll { case cause: Cause[String] =>
        printLine(cause.defects)
      }

  def run: zio.URIO[ZEnv, zio.ExitCode] =
    timeline.exitCode
end Timeline

```


### MalcomInTheMiddle.scala
```scala
 // MalcomInTheMiddle.scala
package cause

import zio.ZEnv
import zio.ZIO

object MalcomInTheMiddle extends zio.App:
  def run(
      args: List[String]
  ): zio.URIO[ZEnv, zio.ExitCode] =

    def turnOnLights() = ???
    class BurntBulb() extends Exception

    def getNewBulb() = ???
    class WobblyShelf() extends Exception

    def grabScrewDriver() = ???
    class SqueakyDrawer() extends Exception

    def sprayWD40() = ???
    class EmptyCan() extends Exception

    def driveToStore() = ???
    class DeadCar() extends Exception

    def repairCar() = ???
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
                              .succeed(
                                println(
                                  "What does it look like I'm doing?!"
                                )
                              )
                              .exitCode
    end try
  end run

/** try { turnOnLights } catch { case
  * burntLightBulb => try {
  */
end MalcomInTheMiddle

```

            