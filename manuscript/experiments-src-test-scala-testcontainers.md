## experiments-src-test-scala-testcontainers

 

### experiments/src/test/scala/testcontainers/ContainerSpec.scala
```scala
package testcontainers
import com.dimafeng.testcontainers.PostgreSQLContainer
import io.github.scottweaver.models.JdbcInfo
import zio.test.*
import zio.*
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer.live
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer.Settings
import io.github.scottweaver.zio.testcontainers.postgres.ZPostgreSQLContainer
import io.github.scottweaver.zio.aspect.DbMigrationAspect
import org.postgresql.ds.PGSimpleDataSource

import java.sql.Connection
import javax.sql.DataSource

object ContainerSpec extends ZIOSpecDefault {
  val testContainerSource: ZLayer[JdbcInfo, Nothing, DataSource] = TestContainerLayers.dataSourceLayer
  val postgres: ZLayer[Settings, Nothing, JdbcInfo & Connection &
    PGSimpleDataSource
      & PostgreSQLContainer] = ZPostgreSQLContainer.live
  def spec =
    (suite("hi")(
    test("there")(
      for {
//        _ <- ZIO.service[DataSource]
        user <- UserService.get("uuid_hard_coded").debug
      } yield assertCompletes
    )
    ) @@ DbMigrationAspect.migrateOnce("db")()).provideShared(
      UserServiceLive.layer,
//      testContainerSource,
      ZPostgreSQLContainer.Settings.default,
      postgres,
    )
}

```


### experiments/src/test/scala/testcontainers/TestContainerLayers.scala
```scala
package testcontainers

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.github.scottweaver.models.JdbcInfo
import zio.*

import java.util.Properties
import javax.sql.DataSource
import scala.jdk.CollectionConverters.MapHasAsJava

object TestContainerLayers {

  val dataSourceLayer: ZLayer[JdbcInfo, Nothing, DataSource] = ZLayer {
    for {
      jdbcInfo   <- ZIO.service[JdbcInfo]
      datasource <- ZIO.attemptBlocking(unsafeDataSourceFromJdbcInfo(jdbcInfo)).orDie
    } yield datasource
  }

  private def unsafeDataSourceFromJdbcInfo(jdbcInfo: JdbcInfo): DataSource = {
    val props = new Properties()
    props.putAll(
      Map(
        "driverClassName" -> jdbcInfo.driverClassName,
        "jdbcUrl"         -> jdbcInfo.jdbcUrl,
        "username"        -> jdbcInfo.username,
        "password"        -> jdbcInfo.password
      ).asJava
    )
    println("JdbcInfo: " + jdbcInfo)
    new HikariDataSource(new HikariConfig(props))
  }
}

```

