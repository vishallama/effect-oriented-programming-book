# Environment Variables

## Historic Approach


Environment Variables are a common way of providing dynamic and/or sensitive data to your running application.
A basic use-case looks like this:

```scala
val apiKey = sys.env.get("API_KEY")
// apiKey: Option[String] = Some("SECRET_API_KEY")
```

This seems rather innocuous; however, it can be an annoying source of problems as your project is built and deployed across different environments.
Given this API:

```scala
trait TravelApi:
  def findCheapestHotel(
      zipCode: String,
      apiKey: String
  ): Either[String, String]
```


Our code could look like this:

```scala
def perfectAnniversaryLodgingUnsafe(
    travelApi: TravelApi
): Either[String, String] =
  val apiKey =
    sys
      .env
      .get("API_KEY")
      .getOrElse(throw new RuntimeException("Unconfigured Environment"))
  travelApi.findCheapestHotel("90210", apiKey)
```

When you look up an Environment Variable, you are accessing information that was _not_ passed in to your function as an explicit argument.
On your own machine, this might work as expected.

```scala
perfectAnniversaryLodgingUnsafe(TravelApiImpl)
// res0: Either[String, String] = Right("Eddy's Roach Motel")
```

However, when your collaborator executes this code on their machine, they might have a different value stored in this variable.


```scala
perfectAnniversaryLodgingUnsafe(TravelApiImpl)
// res2: Either[String, String] = Left("Invalid API Key")
```


```scala
// On a Continuous Integration Server
perfectAnniversaryLodgingUnsafe(TravelApiImpl)
// java.lang.RuntimeException: Unconfigured Environment
// 	at repl.MdocSession$App.$anonfun$1(environment_variables.md:74)
// 	at scala.Option.getOrElse(Option.scala:201)
// 	at repl.MdocSession$App.perfectAnniversaryLodgingUnsafe(environment_variables.md:74)
// 	at repl.MdocSession$App.$init$$$anonfun$1(environment_variables.md:109)
```

## Building a Better Way


Before looking at the official ZIO implementation of `System`, we will create a simpler version.
We need a `trait` that will indicate what is needed from the environment.

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
], Nothing, Either[String, String]] =
  for
    apiKey <- System.env("API_KEY")
  yield TravelApiImpl.findCheapestHotel(
    "90210",
    apiKey.getOrElse(throw new RuntimeException("Unconfigured Environment"))
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
// res5: Either[String, String] = Right("Eddy's Roach Motel")
```

When constructed this way, it becomes very easy to test.
We create a second implementation that accepts test values and serves them to the caller.

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
// res6: Either[String, String] = Left("Invalid API Key")
```


## Official ZIO Approach

ZIO provides a more complete `System` API in the `zio.System`

TODO

```scala
import zio.System

def perfectAnniversaryLodgingZ(): ZIO[Has[
  System
], Nothing, Either[String, String]] =
  for
    apiKey <- System.env("API_KEY")
  yield TravelApiImpl.findCheapestHotel(
    "90210",
    apiKey.get // unsafe!
  )
```