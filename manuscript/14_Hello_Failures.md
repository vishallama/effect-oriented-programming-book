# Hello Failures

If you are not interested in the discouraged ways to handle errors, and just want to see the ZIO approach, jump down to 
[ZIO Error Handling](#zio-error-handling)

## Historic approaches to Error-handling
In the past, some programs have thrown exceptions to indicate failures.
Imagine a program that displays the local temperature the user based on GPS position and a network call. There are distinct levels of problems in any given program. They require different types of handling by the programmer.

```text
Temperature: 30 degrees
```

```scala
class GpsException()     extends RuntimeException
class NetworkException() extends RuntimeException

enum Scenario:
  case Success,
    NetworkError,
    GPSError

def displayTemperature(
    behavior: Scenario
): String =
  if (behavior == Scenario.GPSError)
    throw new GpsException()
  else if (behavior == Scenario.NetworkError)
    throw new NetworkException()
  else
    "35 degrees"
```

```scala
def currentTemperatureUnsafe(
    behavior: Scenario
): String =
  "Temperature: " + displayTemperature(behavior)

currentTemperatureUnsafe(Scenario.Success)
// res0: String = "Temperature: 35 degrees"
```

On the happy path, everything looks as desired.
If the network is unavailable, what is the behavior for the caller?
This can take many forms.
If we don't make any attempt to handle our problem, the whole program blows up and shows the gory details to the user.

```scala
currentTemperatureUnsafe(Scenario.NetworkError)
// repl.MdocSession$App$NetworkException
// 	at repl.MdocSession$App.displayTemperature(14_Hello_Failures.md:25)
// 	at repl.MdocSession$App.currentTemperatureUnsafe(14_Hello_Failures.md:35)
// 	at repl.MdocSession$App.$init$$$anonfun$1(14_Hello_Failures.md:46)
```

We could take the bare-minimum approach of catching the `Exception` and returning `null`:

```scala
def currentTemperatureNull(
    behavior: Scenario
): String =
  try
    "Temperature: " +
      displayTemperature(behavior)
  catch
    case (ex: RuntimeException) =>
      "Temperature: " + null

currentTemperatureNull(Scenario.NetworkError)
// res1: String = "Temperature: null"
```

This is *slightly* better, as the user can at least see the outer structure of our UI element, but it still leaks out code-specific details world.

Maybe we could fallback to a `sentinel` value, such as `0` or `-1` to indicate a failure?

```scala
def currentTemperature(
    behavior: Scenario
): String =
  try
    "Temperature: " +
      displayTemperature(behavior)
  catch
    case (ex: RuntimeException) =>
      "Temperature: -1 degrees"

currentTemperature(Scenario.NetworkError)
// res2: String = "Temperature: -1 degrees"
```

Clearly, this isn't acceptable, as both of these common sentinel values are valid temperatures.
We can take a more honest and accurate approach in this situation.

```scala
def currentTemperature(
    behavior: Scenario
): String =
  try
    "Temperature: " +
      displayTemperature(behavior)
  catch
    case (ex: RuntimeException) =>
      "Temperature Unavailable"

currentTemperature(Scenario.NetworkError)
// res3: String = "Temperature Unavailable"
```

We have improved the failure behavior significantly; is it sufficient for all cases?
Imagine our network connection is stable, but we have a problem in our GPS hardware.
In this situation, do we show the same message to the user? Ideally, we would show the user a distinct message for each scenario.
The Network issue is transient, but the GPS problem is likely permanent.

```scala
def currentTemperature(
    behavior: Scenario
): String =
  try
    "Temperature: " +
      displayTemperature(behavior)
  catch
    case (ex: NetworkException) =>
      "Network Unavailable"
    case (ex: GpsException) =>
      "GPS problem"

currentTemperature(Scenario.NetworkError)
// res4: String = "Network Unavailable"
currentTemperature(Scenario.GPSError)
// res5: String = "GPS problem"
```

Wonderful!
We have specific messages for all relevant error cases. However, this still suffers from downsides that become more painful as the codebase grows.

- The signature of `currentTemperature` does not alert us that it might fail
- If we realize it can fail, we must dig through the implementation to discover the multiple failure values
- We never have certainty about the failure paths of our full application, or any subset of it.

{{ TODO Tear apart exceptions more }}

Encountering an error during a function call generally means two things:

1. You can't continue executing the function in the normal fashion.

2. You can't return a normal result.

Many languages use *exceptions* for handling errors.
An exception *throws* out of the current execution path to locate a user-written *handler* to deal with the error.
There are two goals for exceptions:

1. Separate error-handling code from "success-path" code, so the success-path code is easier to understand and reason about.

2. Reduce redundant error-handling code by handling associated errors in a single place.

Exceptions have problems:

1. They can be "swallowed."
   Just because code throws an exception, there's no guarantee that issue will be dealt with.

1. They can lose important information.
   Once an exception is caught, it is considered to be "handled," and the program doesn't need to retain the failure information.

1. They aren't typed.
   Java's checked exceptions provide a small amount of type information, but it's not that helpful compared to a full type system.
   Unchecked exceptions provide no information at all.

1. Because they are handled dynamically, the only way to ensure your program
   won't crash is by testing it through all possible execution paths. A
   statically-typed error management solution can ensure---at compile
   time---that all errors are handled.

1. They don't scale.
   {{Need to think about this more to make the case.}}

1. Hard to reason about. {{Also need to make this case}}

1. Difficult or impossible to retry an operation if it fails.
   Java {{and Scala?}} use the "termination" model of exception handling.
   This assumes the error is so critical there's no way to get back to where the exception occurred.
   If you're performing an operation that you'd like to retry if it fails, exceptions don't help much.

Exceptions were a valiant attempt to produce a consistent error-reporting interface, and they are definitely better than what's in C.
But they don't end up solving the problem very well, and you just don't know what you're going to get when you use exceptions.


### What's wrong with Try?

### ADTS as another step forward

## ZIO Error Handling

Now we will explore how ZIO enables more powerful, uniform error-handling.

TODO {{Update verbiage now that ZIO section is first}}

- [ZIO Error Handling](#zio-error-handling)
- [Wrapping Legacy Code](#wrapping-legacy-code)

### ZIO-First Error Handling

```scala
import zio.ZIO
import zio.Runtime.default.unsafeRun

def getTemperatureZ(behavior: Scenario): ZIO[
  Any,
  GpsException | NetworkException,
  String
] =
  if (behavior == Scenario.GPSError)
    ZIO.fail(new GpsException())
  else if (behavior == Scenario.NetworkError)
    // TODO Use a non-exceptional error
    ZIO.fail(new NetworkException())
  else
    ZIO.succeed("30 degrees")

unsafeRun(getTemperatureZ(Scenario.Success))
// res6: String = "30 degrees"
```

```scala
unsafeRun(
  getTemperatureZ(Scenario.Success).catchAll {
    case ex: NetworkException =>
      ZIO.succeed("Network Unavailable")
  }
)
// error:
// match may not be exhaustive.
// 
// It would fail on pattern case: _: GpsException
// 
//         ZIO.succeed("Network Unavailable")
//     ^
```

TODO Demonstrate ZIO calculating the error types without an explicit annotation being provided

```scala
unsafeRun(getTemperatureZ(Scenario.GPSError))
// Exception in thread "zio-fiber-10018" repl.MdocSession$App$GpsException: repl.MdocSession$App$GpsException
// 	at repl.MdocSession$.App.<local App>.getTemperatureZ.macro(14_Hello_Failures.md:148)
```

### Wrapping Legacy Code

If we are unable to re-write the fallible function, we can still wrap the call
We are re-using the  `displayTemperature`

{{TODO }}

```scala
import zio.Runtime.default.unsafeRun
import zio.{Task, ZIO}
```

```scala
def displayTemperatureZWrapped(
    behavior: Scenario
): ZIO[Any, Nothing, String] =
  ZIO
    .attempt(displayTemperature(behavior))
    .catchAll {
      case ex: NetworkException =>
        ZIO.succeed("Network Unavailable")
      case ex: GpsException =>
        ZIO.succeed("GPS problem")
    }
```

```scala
unsafeRun(
  displayTemperatureZWrapped(Scenario.Success)
)
// res8: String = "35 degrees"
```

```scala
unsafeRun(
  displayTemperatureZWrapped(
    Scenario.NetworkError
  )
)
// res9: String = "Network Unavailable"
```

This is decent, but does not provide the maximum possible guarantees. Look at what happens if we forget to handle one of our errors.

```scala
def getTemperatureZGpsGap(
    behavior: Scenario
): ZIO[Any, Nothing, String] =
  ZIO
    .attempt(displayTemperature(behavior))
    .catchAll { case ex: NetworkException =>
      ZIO.succeed("Network Unavailable")
    }
```

```scala
import mdoc.unsafeRunTruncate
unsafeRunTruncate(
  getTemperatureZGpsGap(Scenario.GPSError)
)
// res10: String | Unit | String = "bloop"
```

The compiler does not catch this bug, and instead fails at runtime. 


{{TODO show catchSome}}