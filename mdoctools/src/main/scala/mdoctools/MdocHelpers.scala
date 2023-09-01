package mdoctools

import zio.Runtime.default.unsafe

object Stuff:
  object WithALongName:
    object ThatWillComplicate:
      def run =
        throw new Exception(
          "Boom stoinky kablooey pow pow pow"
        )

val commentPrefix = "// "
val columnWidth =
  49 -
    commentPrefix
      .length // TODO Pull from scalafmt config file

private def renderThrowable(
    error: Throwable
): String =
  error
    .toString
    .split("\n")
    .map(line =>
      if (line.length > columnWidth)
        throw new Exception(
          "Need to handle stacktrace line: " +
            line
        )
      else
        line
    )
    .mkString("\n")

// Consider crashing if output is unexpectedly long
def wrapUnsafeZIOReportError[E, A](
    z: => ZIO[Any, E, A]
): ZIO[Any, java.io.IOException, String] =
  val defectPrefix = "Error: "
  val topLineLength =
    columnWidth - defectPrefix.length
  z.map(result => result.toString)
    .catchAll {
      case error: Throwable =>
        ZIO.succeed(renderThrowable(error))
      case error: E =>
        val extractedMessage = error.toString
        val formattedMsg =
          if (
            extractedMessage.length >
              topLineLength
          )
            extractedMessage.take(topLineLength)
          else
            extractedMessage

        ZIO.succeed(formattedMsg)
    }
    .catchAllDefect(defect =>
      val msg = defect.toString
      val extractedMessage =
        if (msg != null && msg.nonEmpty)
          if (msg.contains("$"))
            msg
              .split("\\$")
              .last
              .replace(")", "")
          else
            msg
        else
          ""
      val formattedMsg =
        if (
          extractedMessage.length > topLineLength
        )
          extractedMessage.take(topLineLength)
        else
          extractedMessage

      ZIO.succeed("Defect: " + formattedMsg)
    )
    .map { result =>
      result
        .split("\n")
        .map(line =>
          if (line.length > columnWidth)
            println(
              "Need to handle long line. \n" +
                "Truncating for now: \n" + line
            )
            line.take(columnWidth)
          else
            line
        )
        .mkString("\n")
      // TODO Respect width limit
    }
    .tap(finalValueToRender =>
      ZIO.debug(finalValueToRender)
    )

end wrapUnsafeZIOReportError

def runDemoValue[E, A](
    z: => ZIO[Any, E, A]
): String =
  Unsafe.unsafe { (u: Unsafe) =>
    given Unsafe = u
    unsafe
      .run(wrapUnsafeZIOReportError(z))
      .getOrThrowFiberFailure()
  }

@annotation.nowarn
def runDemo[E, A](z: => ZIO[Any, E, A]): Unit =
  Unsafe.unsafe { (u: Unsafe) =>
    given Unsafe = u
    unsafe
      .run(wrapUnsafeZIOReportError(z))
      .getOrThrowFiberFailure()
  }

// TODO Make a function that will execute a ZIO test case

import zio.System
import zio.test.*
import zio.test.ReporterEventRenderer.ConsoleEventRenderer

object TestRunnerLocal {
  def runSpecAsApp(
                    spec: Spec[TestEnvironment with Scope, Any],
                    console: Console = Console.ConsoleLive,
                    aspects: Chunk[TestAspect[Nothing, Any, Nothing, Any]] = Chunk.empty,
                    testEventHandler: ZTestEventHandler = ZTestEventHandler.silent
                  )(implicit
                    trace: Trace
                  ): URIO[
    TestEnvironment with Scope,
    Summary
  ] = {

    for {
      runtime <-
        ZIO.runtime[
          TestEnvironment with Scope
        ]

      scopeEnv: ZEnvironment[Scope] = runtime.environment
      perTestLayer = (ZLayer.succeedEnvironment(scopeEnv) ++ liveEnvironment) >>>
        (TestEnvironment.live ++ ZLayer.environment[Scope])

      executionEventSinkLayer = ExecutionEventSink.live(Console.ConsoleLive, ConsoleEventRenderer)
      environment            <- ZIO.environment[Any]
      runner =
        TestRunner(
          TestExecutor
            .default[Any, Any](
              ZLayer.succeedEnvironment(environment),
              perTestLayer,
              executionEventSinkLayer,
              testEventHandler
            )
        )
      randomId <- ZIO.withRandom(Random.RandomLive)(Random.nextInt).map("test_case_" + _)
      summary <-
        runner.run(randomId, aspects.foldLeft(spec)(_ @@ _) @@ TestAspect.fibers)
    } yield summary
  }
}

object ProofOfConcept extends ZIOAppDefault:

  val liveEnvironment: Layer[Nothing, Clock with Console with System with Random] = {
    implicit val trace = Trace.empty
    ZLayer.succeedEnvironment(
      ZEnvironment[Clock, Console, System, Random](
        Clock.ClockLive,
        Console.ConsoleLive,
        System.SystemLive,
        Random.RandomLive
      )
    )
  }
  def runSpec[A](x: ZIO[Any, Nothing, TestResult]) =



    TestRunnerLocal.runSpecAsApp(
        zio.test.test("Default label")(x)
        //        .provide(
        //          ZLayer.environment[TestEnvironment with ZIOAppArgs with Scope] +!+
        //            (liveEnvironment >>> TestEnvironment.live +!+ TestLogger.fromConsole(Console.ConsoleLive))
        //        )
      )
      .provide(
        liveEnvironment,
        TestEnvironment.live,
        Scope.default
      )

  def run =
    val spec: ZIO[Any, Nothing, TestResult] =
      defer:
        val res = ZIO.succeed(43).run
        assertTrue(
          res == 43
        )
    runSpec(
      spec
    )

object HelloSpec extends ZIOSpecDefault:
  def spec =
    test("Hello")(
      defer:
        ZIO.debug("hi").run
        val res = ZIO.succeed(42).run
        assertTrue(
          res == 43
        )
    )

object HelloSpecIndirect extends ZIOSpecDefault:
  def spec =
    test("Hello")(
      for
        _ <- ZIO.debug("hi")
        res <- ZIO.succeed(42)
      yield assertTrue(
        res == 43
      )
    )
