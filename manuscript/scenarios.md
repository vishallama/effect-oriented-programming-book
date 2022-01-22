## scenarios

 

### experiments/src/main/scala/scenarios/CivilEngineering.scala
```scala
package scenarios

import zio.ZIOAppArgs
import zio.{ZIOAppDefault, ZIO}

object CivilEngineering extends ZIOAppDefault:
  trait Company[T]:
    def produceBid(
        projectSpecifications: ProjectSpecifications[
          T
        ]
    ): ProjectBid[T]
  object Companies:
    def operatingIn[T](
        state: State
    ): ZIO[World, Nothing, AvailableCompanies[
      T
    ]] = ???

  trait ProjectSpecifications[T]
  trait LegalRestriction
  case class War(reason: String)
  trait UnfulfilledPromise
  trait ProjectBid[T]

  val run = ???

  val installPowerLine = ???

  case class AvailableCompanies[T](
      companies: Set[Company[T]]
  ):
    def lowestBid(
        projectSpecifications: ProjectSpecifications[
          T
        ]
    ): ProjectBid[T] = ???

  trait World
  object World:
    def legalRestrictionsFor(
        state: State
    ): ZIO[World, War, Set[LegalRestriction]] =
      ???
    def politiciansOf(
        state: State
    ): ZIO[World, War, Set[LegalRestriction]] =
      ???

  trait OutOfMoney

  trait PrivatePropertyRefusal
  def build[T](projectBid: ProjectBid[T]): ZIO[
    Any,
    UnfulfilledPromise |
      OutOfMoney |
      PrivatePropertyRefusal,
    T
  ] = ???

  def stateBid[T](
      state: State,
      projectSpecifications: ProjectSpecifications[
        T
      ]
  ): ZIO[
    World,
    War |
      UnfulfilledPromise |
      OutOfMoney |
      PrivatePropertyRefusal,
    T
  ] =
    for
      availableCompanies <-
        Companies.operatingIn[T](state)
      legalRestrictions <-
        World.legalRestrictionsFor(state)
      politicians <- World.politiciansOf(state)
      lowestBid =
        availableCompanies
          .lowestBid(projectSpecifications)
      completedProject <- build(lowestBid)
    yield completedProject
end CivilEngineering

enum State:
  case TX, CO, CA

def buildABridge() =
  trait Company[T]
  trait Surveyor
  trait CivilEngineer
  trait ProjectSpecifications
  trait Specs[Service]
  trait LegalRestriction

  trait ProjectBid
  trait InsufficientResources

  def createProjectSpecifications(): ZIO[
    Any,
    LegalRestriction,
    ProjectSpecifications
  ] = ???

  case class AvailableCompanies[T](
      companies: Set[Company[T]]
  )

  trait Concrete
  trait Steel
  trait UnderWaterDrilling

  trait ConstructionFirm:
    def produceBid(
        projectSpecifications: ProjectSpecifications
    ): ZIO[AvailableCompanies[
      Concrete
    ] & AvailableCompanies[Steel] & AvailableCompanies[UnderWaterDrilling], InsufficientResources, ProjectBid]

  trait NoValidBids

  def chooseConstructionFirm(
      firms: Set[ConstructionFirm]
  ): ZIO[Any, NoValidBids, ConstructionFirm] =
    ???
end buildABridge

```


### experiments/src/main/scala/scenarios/PhonyZIO.scala
```scala
package atomic

case class Schedule()

trait ZIO[R, E, A]:
  def map[B](f: A => B): ZIO[R, E, B] = ???

  def flatMap[R2, E2, B](
      f: A => ZIO[R2, E2, B]
  ): ZIO[R, E, B] = ???

  def retry(schedule: Schedule): ZIO[R, E, A] =
    ???

  def catchAll(
      handler: (E => A)
  ): ZIO[R, Nothing, A] = ???

case class UIO[A]() extends ZIO[Any, Nothing, A]

case class URIO[R, A]()
    extends ZIO[R, Nothing, A]

case class Task[A]()
    extends ZIO[Any, Throwable, A]

case class RIO[R, A]()
    extends ZIO[R, Throwable, A]

case class IO[E <: Throwable, A]()
    extends ZIO[Any, E, A]

object ZIO:

  def apply[T](
      body: => T
  ): ZIO[Any, Nothing, T] = ???

trait Has[A]

```


### experiments/src/main/scala/scenarios/SecuritySystem.scala
```scala
package scenarios

import zio.{ZIO, ZLayer}
import zio.Clock
import zio.Duration
import zio.Console.printLine
import zio.durationInt
import zio.Schedule
import scala.concurrent.TimeoutException
import time.scheduledValues

case class TempSense(
    z: ZIO[
      Clock,
      HardwareFailure,
      ZIO[Clock, TimeoutException, Degrees]
    ]
)

/** Situations: Security System: Should monitor
  *   - Motion
  *   - Heat/Infrared
  *   - Sound Should alert by:
  *   - Quiet, local beep
  *   - Loud Local Siren
  *   - Ping security company
  *   - Notify police
  */
object SecuritySystem:
  // TODO Why can't I use this???
  val s: zio.ZLayer[
    Any,
    Nothing,
    scenarios.TempSense
  ] =
    SensorData.live[Degrees, TempSense](
      x => TempSense(x),
      (1.seconds, Degrees(71)),
      (2.seconds, Degrees(70))
    )

  val fullServiceBuilder: ZLayer[
    Any,
    Nothing,
    scenarios.MotionDetector &
      scenarios.ThermalDetectorX &
      AcousticDetectorX &
      SirenX
  ] =
    MotionDetector.live ++
      ThermalDetectorX(
        (1.seconds, Degrees(71)),
        (1.seconds, Degrees(70)),
        (3.seconds, Degrees(98))
      ) // ++ s
    ++
    AcousticDetectorX(
      (4.seconds, Decibels(11)),
      (1.seconds, Decibels(20))
    ) ++ SirenX.live
  end fullServiceBuilder

  val accessMotionDetector: ZIO[
    scenarios.MotionDetector,
    scenarios.HardwareFailure,
    scenarios.Pixels
  ] =
    ZIO
      .environmentWithZIO(_.get.amountOfMotion())

  def securityLoop(
      amountOfHeatGenerator: ZIO[
        Clock,
        scala.concurrent.TimeoutException |
          scenarios.HardwareFailure,
        scenarios.Degrees
      ],
      amountOfMotion: Pixels,
      acousticDetector: ZIO[
        Clock,
        scala.concurrent.TimeoutException |
          scenarios.HardwareFailure,
        scenarios.Decibels
      ]
  ): ZIO[
    Clock & SirenX,
    scala.concurrent.TimeoutException |
      HardwareFailure,
    Unit
  ] =
    for
      amountOfHeat <- amountOfHeatGenerator
      noise        <- acousticDetector
      _ <-
        ZIO.debug(
          s"Heat: $amountOfHeat  Motion: $amountOfMotion  Noise: $noise"
        )
      securityResponse =
        determineResponse(
          amountOfMotion,
          amountOfHeat,
          noise
        )
      _ <-
        securityResponse match
          case Relax =>
            ZIO.debug("No need to panic")
          case LowBeep =>
            SirenX.lowBeep
          case LoudSiren =>
            SirenX.loudSiren
    yield ()

  def shouldAlertServices(): ZIO[
    MotionDetector &
      ThermalDetectorX &
      SirenX &
      AcousticDetectorX &
      Clock,
    scenarios.HardwareFailure | TimeoutException,
    String
  ] =
    for
      amountOfMotion <-
        MotionDetector
          .acquireMotionMeasurementSource()
      amountOfHeatGenerator <-
        ThermalDetectorX
          .acquireHeatMeasurementSource
      acousticDetector <-
        AcousticDetectorX.acquireDetector
      _ <-
        securityLoop(
          amountOfHeatGenerator,
          amountOfMotion,
          acousticDetector
        ).repeat(
          Schedule.recurs(5) &&
            Schedule.spaced(1.seconds)
        )
    yield "Fin"

  def shouldTrigger(
      amountOfMotion: Pixels,
      amountOfHeat: Degrees
  ): Boolean =
    amountOfMotion.value > 10 &&
      amountOfHeat.value > 95

  def determineResponse(
      amountOfMotion: Pixels,
      amountOfHeat: Degrees,
      noise: Decibels
  ): SecurityResponse =
    val numberOfAlerts =
      List(
        amountOfMotion.value > 50,
        amountOfHeat.value > 95,
        noise.value > 15
      ).filter(_ == true).length

    if (numberOfAlerts == 0)
      Relax
    else if (numberOfAlerts == 1)
      LowBeep
    else
      LoudSiren
  end determineResponse

  def determineBreaches(
      amountOfMotion: Pixels,
      amountOfHeat: Degrees,
      noise: Decibels
  ): Set[SecurityBreach] =
    List(
      Option.when(amountOfMotion.value > 50)(
        SignificantMotion
      ),
      Option.when(
        amountOfHeat.value > 95 &&
          amountOfHeat.value < 200
      )(BodyHeat),
      Option
        .when(amountOfHeat.value >= 200)(Fire),
      Option.when(noise.value > 15)(LoudNoise)
    ).flatten.toSet

end SecuritySystem

trait SecurityBreach
object BodyHeat          extends SecurityBreach
object Fire              extends SecurityBreach
object LoudNoise         extends SecurityBreach
object SignificantMotion extends SecurityBreach

trait SecurityResponse
object Relax     extends SecurityResponse
object LowBeep   extends SecurityResponse
object LoudSiren extends SecurityResponse

@main
def useSecuritySystem =
  import zio.Runtime.default.unsafeRun
  println(
    "Final result: " +
      unsafeRun(
        SecuritySystem
          .shouldAlertServices()
          .provide(
            SecuritySystem.fullServiceBuilder ++
              Clock.live
          )
          .catchSome {
            case _: TimeoutException =>
              printLine(
                "Invalid Scenario. Ran out of sensor data."
              )
          }
      )
  )
end useSecuritySystem

trait HardwareFailure

case class Decibels(value: Int)
case class Degrees(value: Int)
case class Pixels(value: Int)

trait MotionDetector:
  def amountOfMotion()
      : ZIO[Any, HardwareFailure, Pixels]

object MotionDetector:

  object LiveMotionDetector
      extends MotionDetector:
    override def amountOfMotion()
        : ZIO[Any, HardwareFailure, Pixels] =
      ZIO.succeed(Pixels(30))

  def acquireMotionMeasurementSource(): ZIO[
    MotionDetector,
    HardwareFailure,
    Pixels
  ] =
    ZIO
      .service[MotionDetector]
      .flatMap(_.amountOfMotion())

  val live
      : ZLayer[Any, Nothing, MotionDetector] =
    ZLayer.succeed(LiveMotionDetector)
end MotionDetector

trait ThermalDetectorX:
  def heatMeasurementSource()
      : ZIO[Clock, Nothing, ZIO[
        Clock,
        TimeoutException |
          scenarios.HardwareFailure,
        Degrees
      ]]

object ThermalDetectorX:

  def apply(
      value: (Duration, Degrees),
      values: (Duration, Degrees)*
  ): ZLayer[Any, Nothing, ThermalDetectorX] =
    ZLayer.succeed(
      // that same service we wrote above
      new ThermalDetectorX:
        override def heatMeasurementSource()
            : ZIO[Clock, Nothing, ZIO[
              Clock,
              TimeoutException |
                scenarios.HardwareFailure,
              Degrees
            ]] = scheduledValues(value, values*)
    )

  // This is preeeetty gnarly. How can we
  // improve?
  val acquireHeatMeasurementSource: ZIO[
    scenarios.ThermalDetectorX & Clock,
    Nothing,
    ZIO[
      Clock,
      scala.concurrent.TimeoutException |
        scenarios.HardwareFailure,
      scenarios.Degrees
    ]
  ] =
    ZIO.environmentWithZIO[
      scenarios.ThermalDetectorX & Clock
    ](
      _.get[scenarios.ThermalDetectorX]
        .heatMeasurementSource()
    )

end ThermalDetectorX

trait AcousticDetectorX:
  def acquireDetector(): ZIO[Clock, Nothing, ZIO[
    Clock,
    TimeoutException | scenarios.HardwareFailure,
    Decibels
  ]]

object AcousticDetectorX:

  def apply(
      value: (Duration, Decibels),
      values: (Duration, Decibels)*
  ): ZLayer[Any, Nothing, AcousticDetectorX] =
    ZLayer.succeed(
      // that same service we wrote above
      new AcousticDetectorX:
        override def acquireDetector()
            : ZIO[Clock, Nothing, ZIO[
              Clock,
              TimeoutException |
                scenarios.HardwareFailure,
              Decibels
            ]] = scheduledValues(value, values*)
    )

  // This is preeeetty gnarly. How can we
  // improve?
  val acquireDetector: ZIO[
    scenarios.AcousticDetectorX & Clock,
    Nothing,
    ZIO[
      Clock,
      scala.concurrent.TimeoutException |
        scenarios.HardwareFailure,
      scenarios.Decibels
    ]
  ] =
    ZIO.environmentWithZIO[
      scenarios.AcousticDetectorX & Clock
    ](
      _.get[scenarios.AcousticDetectorX]
        .acquireDetector()
    )

end AcousticDetectorX

object Siren:
  trait ServiceX:
    def lowBeep(): ZIO[
      Any,
      scenarios.HardwareFailure,
      Unit
    ]

  val live
      : ZLayer[Any, Nothing, Siren.ServiceX] =
    ZLayer.succeed(
      // that same service we wrote above
      new ServiceX:

        def lowBeep(): ZIO[
          Any,
          scenarios.HardwareFailure,
          Unit
        ] = ZIO.debug("beeeeeeeeeep")
    )
end Siren

trait SirenX:
  def lowBeep()
      : ZIO[Any, scenarios.HardwareFailure, Unit]

  def loudSiren()
      : ZIO[Any, scenarios.HardwareFailure, Unit]

object SirenX:
  object SirenXLive extends SirenX:
    def lowBeep(): ZIO[
      Any,
      scenarios.HardwareFailure,
      Unit
    ] = ZIO.debug("beeeeeeeeeep")

    def loudSiren(): ZIO[
      Any,
      scenarios.HardwareFailure,
      Unit
    ] = ZIO.debug("WOOOO EEEE WOOOOO EEEE")

  val live: ZLayer[Any, Nothing, SirenX] =
    ZLayer.succeed(SirenXLive)

  val lowBeep: ZIO[
    SirenX,
    scenarios.HardwareFailure,
    Unit
  ] = ZIO.serviceWith(_.lowBeep())

  val loudSiren: ZIO[
    SirenX,
    scenarios.HardwareFailure,
    Unit
  ] = ZIO.serviceWith(_.loudSiren())

end SirenX

class SensorD[T](
    z: ZIO[
      Clock,
      HardwareFailure,
      ZIO[Clock, TimeoutException, T]
    ]
)

// TODO Figure out how to use this
object SensorData:
  def live[T, Y: zio.Tag: zio.IsNotIntersection](
      c: ZIO[
        Clock,
        HardwareFailure,
        ZIO[Clock, TimeoutException, T]
      ] => Y,
      value: (Duration, T),
      values: (Duration, T)*
  ): ZLayer[Any, Nothing, Y] =
    ZLayer.succeed(
      // that same service we wrote above
      c(scheduledValues[T](value, values*))
    )

  def liveS[T: zio.Tag](
      value: (Duration, T),
      values: (Duration, T)*
  ): ZLayer[Any, Nothing, SensorD[T]] =
    ZLayer.succeed(
      // that same service we wrote above
      SensorD(scheduledValues[T](value, values*))
    )
end SensorData

```

