# Environment Variables


Environment Variables are a common way of providing dynamic and/or sensitive data to your running application.
A basic use-case looks like this:

```scala
val apiKey = sys.env.get("API_KEY")
// apiKey: Option[String] = Some("SECRET_API_KEY")
```

This seems rather innocuous; however, it can be an annoying source of problems as your project is built and deployed across different environments.


```scala
def findPerfectAnniversaryLodging() =
  val apiKey =
    sys
      .env
      .get("API_KEY")
      .getOrElse(
        throw new RuntimeException("boom")
      )
  TravelServiceApi
    .findCheapestHotel("90210", apiKey)

findPerfectAnniversaryLodging()
// res0: Option[String] = Some("Bargain Eddy's Roach Motel")
```

When you look up an Environment Variable, you are accessing information that was _not_ passed in to your function as an explicit argument.

