# Environment Variables

## Historic Approach


Environment Variables are a common way of providing dynamic and/or sensitive data to your running application.
A basic use-case looks like this:

```scala
val apiKey = sys.env.get("API_KEY")
// apiKey: Option[String] = Some("SECRET_API_KEY")
```

This seems rather innocuous; however, it can be an annoying source of problems as your project is built and deployed across different environments.


```scala
def perfectAnniversaryLodging()
    : Either[String, String] =
  val apiKey =
    sys
      .env
      .get("API_KEY")
      .get // Unsafe, but useful for demo
  TravelServiceApi
    .findCheapestHotel("90210", apiKey)

perfectAnniversaryLodging()
// res0: Either[String, String] = Right("Eddy's Roach Motel")
```

When you look up an Environment Variable, you are accessing information that was _not_ passed in to your function as an explicit argument.

## Building a Better Way

Before looking at the official ZIO implementation, we will create a simpler version.
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
  yield TravelServiceApi.findCheapestHotel(
    "90210",
    apiKey.get // unsafe!
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
// res1: Either[String, String] = Right("Eddy's Roach Motel")
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
// res2: Either[String, String] = Left("Invalid API Key")
```


## Official ZIO Approach

TODO

```scala
import zio.System

def perfectAnniversaryLodgingZ(): ZIO[Has[
  System
], Nothing, Either[String, String]] =
  for
    apiKey <- System.env("API_KEY")
  yield TravelServiceApi.findCheapestHotel(
    "90210",
    apiKey.get // unsafe!
  )
```