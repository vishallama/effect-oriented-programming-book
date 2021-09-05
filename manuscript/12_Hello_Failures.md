# Hello Failures

If you are not interested in the discouraged ways to handle errors, and just want to see the ZIO approach, jump down to 
[ZIO Error Handling](#zio-error-handling)

## Historic approaches to Error-handling

There are distinct levels of problems in any given program. They require different types of handling by the programmer. Imagine a program that displays the local temperature the user based on GPS position and a network call.

TODO Show success/failure for all versions

```text
Temperature: 30 degrees
```

```scala
class GpsException()     extends RuntimeException
class NetworkException() extends RuntimeException

def getTemperature(behavior: String): String =
  if (behavior == "GPS Error")
    throw new GpsException()
  else if (behavior == "Network Error")
    throw new NetworkException()
  else
    "35 degrees"
```

```scala
def displayTemperatureUnsafe(
    behavior: String
): String =
  "Temperature: " + getTemperature(behavior)

displayTemperatureUnsafe("succeed")
// res0: String = "Temperature: 35 degrees"
```

On the happy path, everything looks as desired.
If the network is unavailable, what is the behavior for the caller?
This can take many forms.
If we don't make any attempt to handle our problem, the whole program could blow up and show the gory details to the user.

```scala
displayTemperatureUnsafe("Network Error")
// repl.MdocSession$App$NetworkException
// 	at repl.MdocSession$App.getTemperature(12_Hello_Failures.md:17)
// 	at repl.MdocSession$App.displayTemperatureUnsafe(12_Hello_Failures.md:27)
// 	at repl.MdocSession$App.$init$$$anonfun$1(12_Hello_Failures.md:38)
```

We could take the bare-minimum approach of catching the `Exception` and returning `null`:

```scala
def displayTemperatureNull(
    behavior: String
): String =
  try
    "Temperature: " + getTemperature(behavior)
  catch
    case (ex: RuntimeException) =>
      "Temperature: " + null

displayTemperatureNull("Network Error")
// res1: String = "Temperature: null"
```

This is *slightly* better, as the user can at least see the outer structure of our UI element, but it still leaks out code-specific details world.

Maybe we could fallback to a `sentinel` value, such as `0` or `-1` to indicate a failure?

```scala
def displayTemperature(
    behavior: String
): String =
  try
    "Temperature: " + getTemperature(behavior)
  catch
    case (ex: RuntimeException) =>
      "Temperature: -1 degrees"

displayTemperature("Network Error")
// res2: String = "Temperature: -1 degrees"
```

Clearly, this isn't acceptable, as both of these common sentinel values are valid temperatures.
We can take a more honest and accurate approach in this situation.

```scala
def displayTemperature(
    behavior: String
): String =
  try
    "Temperature: " + getTemperature(behavior)
  catch
    case (ex: RuntimeException) =>
      "Temperature Unavailable"

displayTemperature("Network Error")
// res3: String = "Temperature Unavailable"
```

We have improved the failure behavior significantly; is it sufficient for all cases?
Imagine our network connection is stable, but we have a problem in our GPS hardware.
In this situation, do we show the same message to the user? Ideally, we would show the user a distinct message for each scenario.
The Network issue is transient, but the GPS problem is likely permanent.

```scala
def displayTemperature(
    behavior: String
): String =
  try
    "Temperature: " + getTemperature(behavior)
  catch
    case (ex: NetworkException) =>
      "Network Unavailable"
    case (ex: GpsException) =>
      "GPS problem"

displayTemperature("Network Error")
// res4: String = "Network Unavailable"
displayTemperature("GPS Error")
// res5: String = "GPS problem"
```

Wonderful!
We have specific messages for all relevant error cases. However, this still suffers from downsides that become more painful as the codebase grows.

- The signature of `getTemperature` does not alert us that it might fail
- If we realize it can fail, we must dig through the implementation to discover the multiple failure values

## ZIO Error Handling

Now we will explore how ZIO enables more powerful, uniform error-handling.

TODO {{Update verbiage now that ZIO section is first}}

- [ZIO Error Handling](#zio-error-handling)
- [Wrapping Legacy Code](#wrapping-legacy-code)

### ZIO-First Error Handling

```scala
import zio.ZIO
import zio.Runtime.default.unsafeRun

def getTemperatureZ(behavior: String): ZIO[
  Any,
  GpsException | NetworkException,
  String
] =
  if (behavior == "GPS Error")
    ZIO.fail(new GpsException())
  else if (behavior == "Network Error")
    // TODO Use a non-exceptional error
    ZIO.fail(new NetworkException())
  else
    ZIO.succeed("30 degrees")

unsafeRun(getTemperatureZ("Succeed"))
// res6: String = "30 degrees"
```

```scala
unsafeRun(
  getTemperatureZ("Succeed").catchAll {
    case ex: NetworkException =>
      ZIO.succeed("Network Unavailable")
  }
)
// error: 
// match may not be exhaustive.
// 
// It would fail on pattern case: _: GpsException
//
```

TODO Demonstrate ZIO calculating the error types without an explicit annotation being provided

```scala
unsafeRun( getTemperatureZ("GPS Error") )
// zio.FiberFailure: Fiber failed.
// A checked error was not handled.
// repl.MdocSession$App$GpsException
// 	at repl.MdocSession$App.getTemperatureZ$1$$anonfun$1(12_Hello_Failures.md:136)
// 	at zio.ZIO$.fail$$anonfun$1(ZIO.scala:3383)
// 	at zio.internal.FiberContext.runUntil(FiberContext.scala:448)
// 	at zio.internal.FiberContext.run(FiberContext.scala:305)
// 	at zio.Runtime.unsafeRunWith(Runtime.scala:312)
// 	at zio.Runtime.defaultUnsafeRunSync(Runtime.scala:89)
// 	at zio.Runtime.defaultUnsafeRunSync$(Runtime.scala:27)
// 	at zio.Runtime$$anon$3.defaultUnsafeRunSync(Runtime.scala:379)
// 	at zio.Runtime.unsafeRunSync(Runtime.scala:84)
// 	at zio.Runtime.unsafeRunSync$(Runtime.scala:27)
// 	at zio.Runtime$$anon$3.unsafeRunSync(Runtime.scala:379)
// 	at zio.Runtime.unsafeRun(Runtime.scala:66)
// 	at zio.Runtime.unsafeRun$(Runtime.scala:27)
// 	at zio.Runtime$$anon$3.unsafeRun(Runtime.scala:379)
// 	at repl.MdocSession$App.$init$$$anonfun$2(12_Hello_Failures.md:158)
// 	at mdoc.internal.document.DocumentBuilder$$doc$.crash(DocumentBuilder.scala:75)
// 	at repl.MdocSession$App.<init>(12_Hello_Failures.md:159)
// 	at repl.MdocSession$.app(12_Hello_Failures.md:3)
// 	at mdoc.internal.document.DocumentBuilder$$doc$.build$$anonfun$2$$anonfun$1(DocumentBuilder.scala:89)
// 	at scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
// 	at scala.util.DynamicVariable.withValue(DynamicVariable.scala:59)
// 	at scala.Console$.withErr(Console.scala:193)
// 	at mdoc.internal.document.DocumentBuilder$$doc$.build$$anonfun$1(DocumentBuilder.scala:90)
// 	at scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
// 	at scala.util.DynamicVariable.withValue(DynamicVariable.scala:59)
// 	at scala.Console$.withOut(Console.scala:164)
// 	at mdoc.internal.document.DocumentBuilder$$doc$.build(DocumentBuilder.scala:91)
// 	at mdoc.internal.markdown.MarkdownBuilder$.liftedTree1$1(MarkdownBuilder.scala:47)
// 	at mdoc.internal.markdown.MarkdownBuilder$.$anonfun$1(MarkdownBuilder.scala:70)
// 	at mdoc.internal.markdown.MarkdownBuilder$$anon$1.run(MarkdownBuilder.scala:103)
// 
// Fiber:Id(1630880130449,17) was supposed to continue to: <empty trace>
// 
// Fiber:Id(1630880130449,17) ZIO Execution trace: <empty trace>
// 
// Fiber:Id(1630880130449,17) was spawned by: <empty trace>
```

### Wrapping Legacy Code

If we are unable to re-write the fallible function, we can still wrap the call

```scala
import zio.Runtime.default.unsafeRun
import zio.{Task, ZIO}
```

```scala
def getTemperatureZWrapped(
    behavior: String
): ZIO[Any, Throwable, String] =
  ZIO(getTemperature(behavior)).catchAll {
    case ex: NetworkException =>
      ZIO.succeed("Network Unavailable")
    case ex: GpsException =>
      ZIO.succeed("GPS problem")
  }
```

```scala
unsafeRun(getTemperatureZWrapped("Succeed"))
// res8: String = "35 degrees"
```

```scala
unsafeRun(
  getTemperatureZWrapped("Network Error")
)
// res9: String = "Network Unavailable"
```

This is decent, but does not provide the maximum possible guarantees. Look at what happens if we forget to handle one of our errors.

```scala
def getTemperatureZGpsGap(
    behavior: String
): ZIO[Any, Exception, String] =
  ZIO(getTemperature(behavior)).catchAll {
    case ex: NetworkException =>
      ZIO.succeed("Network Unavailable")
  }
import mdoc.unsafeRunTruncate
```

```scala
import mdoc.unsafeRunTruncate
unsafeRunTruncate(
  getTemperatureZGpsGap("GPS Error")
)
// Defect: class scala.MatchError
//         GpsException
```

The compiler does not catch this bug, and instead fails at runtime. Can we do better?
