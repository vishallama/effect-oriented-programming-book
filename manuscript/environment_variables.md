# Environment Variables

## Historic Approach


Environment Variables are a common way of providing dynamic and/or sensitive data to your running application. A basic use-case looks like this:

```scala
val apiKey = sys.env.get("API_KEY")
// apiKey: Option[String] = Some("SECRET_API_KEY")
```

This seems rather innocuous; however, it can be an annoying source of problems as your project is built and deployed across different environments. Given this API:

```scala
trait TravelApi:
  def cheapestHotel(
      zipCode: String,
      apiKey: String
  ): Either[Error, Hotel]

case class Hotel(name: String)
case class Error(msg: String)
```


Our code could look like this:

```scala
def findPerfectLodgingUnsafe(
    travelApi: TravelApi
): Either[Error, Hotel] =
  // TODO How many Option/Either tricks are
  // appropriate?
  for
    apiKey <-
      sys
        .env
        .get("API_KEY")
        .toRight(
          Error("Unconfigured Environment")
        )
    res <-
      travelApi.cheapestHotel("90210", apiKey)
  yield res
```

When you look up an Environment Variable, you are accessing information that was _not_ passed in to your function as an explicit argument. Now we will simulate running the exact same function with the same arguments in 3 different environments.

**Your Machine**

```scala
findPerfectLodgingUnsafe(TravelApiImpl)
// res0: Either[Error, Hotel] = Right(
//   Hotel("Eddy's Roach Motel")
// )
```

**Collaborator's Machine**


```scala
findPerfectLodgingUnsafe(TravelApiImpl)
// res2: Either[Error, Hotel] = Left(
//   Error("Invalid API Key")
// )
```

**Continuous Integration Server**


```scala
findPerfectLodgingUnsafe(TravelApiImpl)
// res4: Either[Error, Hotel] = Left(
//   Error("Unconfigured Environment")
// )
```

On your own machine, everything works as expected. However, your collaborator has a different value stored in this variable, and gets a failure when they execute this code. Finally, the CI server has set _any_ value, and completely blows up at runtime.

## Building a Better Way


Before looking at the official ZIO implementation of `System`, we will create a simpler version. We need a `trait` that will indicate what is needed from the environment.

```scala
import zio.ZIO
trait System:
  def env(
      variable: String
  ): ZIO[Any, Nothing, Option[String]]
```

Now, our live implementation will simply wrap our original, unsafe function call.

```scala
class SystemLive() extends System:
  def env(
      variable: String
  ): ZIO[Any, Nothing, Option[String]] =
    ZIO.succeed(sys.env.get("API_KEY"))
```

Finally, for easier usage by the caller, we create an accessor in the companion object.

```scala
import zio.Has

object System:
  def env(
      variable: => String
  ): ZIO[Has[System], Nothing, Option[String]] =
    ZIO.accessZIO(_.get.env(variable))
```

Now if we use this code, our caller's type signature is forced to tell us that it requires a `System` to execute.

```scala
def perfectAnniversaryLodgingSafe(): ZIO[Has[
  System
], Nothing, Either[Error, Hotel]] =
  for
    apiKey <- System.env("API_KEY")
  yield TravelApiImpl.cheapestHotel(
    "90210",
    apiKey.getOrElse(
      throw new RuntimeException(
        "Unconfigured Environment"
      )
    )
  )
```

This is what it looks like in action:

```scala
import zio.Runtime.default.unsafeRun
import zio.ZLayer

unsafeRun(
  perfectAnniversaryLodgingSafe().provideLayer(
    ZLayer.succeed[System](SystemLive())
  )
)
// res6: Either[Error, Hotel] = Right(Hotel("Eddy's Roach Motel"))
```

When constructed this way, it becomes very easy to test. We create a second implementation that accepts test values and serves them to the caller.

```scala
case class SystemHardcoded(
    environmentVars: Map[String, String]
) extends System:
  def env(
      variable: String
  ): ZIO[Any, Nothing, Option[String]] =
    ZIO.succeed(environmentVars.get(variable))
```

We can now provide this to our logic, for testing both the happy path and failure cases.

```scala
unsafeRun(
  perfectAnniversaryLodgingSafe().provideLayer(
    ZLayer.succeed[System](
      SystemHardcoded(
        Map("API_KEY" -> "Invalid Key")
      )
    )
  )
)
// res7: Either[Error, Hotel] = Left(Error("Invalid API Key"))
```

## Official ZIO Approach

ZIO provides a more complete `System` API in the `zio.System`

TODO

```scala
import zio.System

def perfectAnniversaryLodgingZ(): ZIO[Has[
  System
], Nothing, Either[Error, Hotel]] =
  for
    apiKey <- System.env("API_KEY")
  yield TravelApiImpl.cheapestHotel(
    "90210",
    apiKey.get // unsafe!
  )
```

## Exercises

```scala
import zio.test.environment.TestSystem
import zio.test.environment.TestSystem.Data
```

X> **Exercise 1:** Create a function will report missing Environment Variables as `NoSuchElementException` failures, instead of an `Option` success case.

```scala
trait Exercise1:
  def envOrFail(variable: String): ZIO[Has[
    zio.System
  ], SecurityException | NoSuchElementException, String]
```


```scala
val exercise1case1 =
  unsafeRun(
    Exercise1Solution
      .envOrFail("key")
      .provideLayer(
        TestSystem.live(
          Data(envs = Map("key" -> "value"))
        )
      )
  )
// exercise1case1: String = "value"
assert(exercise1case1 == "value")
```

```scala
val exercise1case2 =
  unsafeRun(
    Exercise1Solution
      .envOrFail("key")
      .catchSome {
        case _: NoSuchElementException =>
          ZIO.succeed("Expected Error")
      }
      .provideLayer(
        TestSystem.live(Data(envs = Map()))
      )
  )
// exercise1case2: String = "Expected Error"

assert(exercise1case2 == "Expected Error")
```

X> **Exercise 2:** Create a function will attempt to parse a value as an Integer and report errors as a `NumberFormatException`.

```scala
trait Exercise2:
  def envInt(variable: String): ZIO[
    Any,
    NoSuchElementException |
      NumberFormatException,
    Int
  ]
