## testcontainers

 

### experiments/src/main/scala/testcontainers/QuillContext.scala
```scala
package testcontainers

import com.typesafe.config.ConfigFactory
import io.getquill.context.ZioJdbc.DataSourceLayer
import io.getquill.{
  PostgresZioJdbcContext,
  SnakeCase
}
import zio.*

import javax.sql.DataSource
import scala.jdk.CollectionConverters.MapHasAsJava

/** QuillContext houses the datasource layer
  * which initializes a connection pool. This has
  * been slightly complicated by the way Heroku
  * exposes its connection details. Database URL
  * will only be defined when run from Heroku in
  * production.
  */
object QuillContext
    extends PostgresZioJdbcContext(SnakeCase):
  val dataSourceLayer
      : ZLayer[Any, Nothing, DataSource] =
    ZLayer {
      for
        _ <- ZIO.debug("Hi")
        herokuURL <-
          System.env("DATABASE_URL").orDie
        _ <- ZIO.debug("Bye")
        localDBConfig =
          Map(
            "dataSource.user"     -> "postgres",
            "dataSource.password" -> "",
            "dataSource.url" ->
              "jdbc:postgresql://localhost:5432/postgres"
          )
        configMap =
          herokuURL
            .map(parseHerokuDatabaseUrl(_).toMap)
            .getOrElse(localDBConfig)
        config =
          ConfigFactory.parseMap(
            configMap
              .updated(
                "dataSourceClassName",
                "org.postgresql.ds.PGSimpleDataSource"
              )
              .asJava
          )
      yield DataSourceLayer
        .fromConfig(config)
        .orDie
    }.flatten

  /** HerokuConnectionInfo is a wrapper for the
    * datasource information to make it
    * compatible with Heroku
    */
  final case class HerokuConnectionInfo(
      username: String,
      password: String,
      host: String,
      port: String,
      dbname: String
  ):
    def toMap: Map[String, String] =
      Map(
        "dataSource.user"     -> username,
        "dataSource.password" -> password,
        "dataSource.url" ->
          s"jdbc:postgresql://$host:$port/$dbname"
      )

  /** Parses the necessary information out of the
    * Heroku formatted URL
    */
  def parseHerokuDatabaseUrl(
      string: String
  ): HerokuConnectionInfo =
    string match
      case s"postgres://$username:$password@$host:$port/$dbname" =>
        HerokuConnectionInfo(
          username,
          password,
          host,
          port,
          dbname
        )
end QuillContext

```


### experiments/src/main/scala/testcontainers/UserService.scala
```scala
package testcontainers

import zio.*
import javax.sql.DataSource

trait UserNotFound
case class AppUser(userId: String, name: String)

trait UserService:
  def get(
      userId: String
  ): ZIO[DataSource, UserNotFound, AppUser]

object UserService:
  def get(userId: String): ZIO[
    UserService with DataSource,
    UserNotFound,
    AppUser
  ] = // TODO Um? Why Nothing?????
    ZIO.serviceWithZIO[UserService](x =>
      x.get(userId)
    )

final case class UserServiceLive(
    dataSource: DataSource
) extends UserService:
  import io.getquill._
  // SnakeCase turns firstName -> first_name
  println("A")
  val ctx = new PostgresZioJdbcContext(SnakeCase)
  println("B")
  import ctx._

  def get(
      userId: String
  ): ZIO[DataSource, UserNotFound, AppUser] =
    inline def somePeople =
      quote {
        query[AppUser]
          .filter(_.userId == lift(userId))
      }
    run(somePeople)
      .provideEnvironment(
        ZEnvironment(dataSource)
      )
      .orDie
      .map(_.head)
end UserServiceLive

object UserServiceLive:
  val layer: URLayer[DataSource, UserService] =
    ZLayer.fromFunction(UserServiceLive.apply _)
//    ZLayer.fromZIO(
//    for {
//      ds <- ZIO.service[DataSource]
//    } yield UserServiceLive(ds)
//    )
//    ZLayer.fromFunction()

```

