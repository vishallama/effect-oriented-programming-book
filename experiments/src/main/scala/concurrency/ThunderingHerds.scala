package concurrency
import zio.*
import zio.Console.printLine
import zio.direct.*

import java.nio.file.Path

case class FileContents(contents: List[String])

trait FileService:
  def retrieveContents(
      name: Path
  ): ZIO[Any, Nothing, FileContents]

  val hits: ZIO[Any, Nothing, Int]

  val misses: ZIO[Any, Nothing, Int]

object FileService:
  val live =
    ZLayer.fromZIO(
      defer {
        val fs   = ZIO.service[FileSystem].run
        val hit  = Ref.make[Int](0).run
        val miss = Ref.make[Int](0).run
        val cache =
          Ref.make[Map[Path, FileContents]](
            Map.empty
          ).run
        val activeRefreshes =
          Ref.make[Map[Path, ActiveUpdate]](
            Map.empty
          ).run
        Live(
          hit,
          miss,
          cache,
          activeRefreshes,
          fs
        )
      }
    )

  case class ActiveUpdate(
      observers: Int,
      promise: Promise[Nothing, FileContents]
  )

  case class Live(
      hit: Ref[Int],
      miss: Ref[Int],
      // TODO Consider ConcurrentMap
      cache: Ref[Map[Path, FileContents]],
      activeRefresh: Ref[
        Map[Path, ActiveUpdate]
      ],
      fileSystem: FileSystem
  ) extends FileService:

    def retrieveContents(
        name: Path
    ): ZIO[Any, Nothing, FileContents] =
      for
        cachedValue <- cache.get.map(_.get(name))
        activeValue <-
          cachedValue match
            case Some(initValue) =>
              hit.update(_ + 1) *>
                printLine(
                  "Value was cached. Easy path."
                ).orDie *> ZIO.succeed(initValue)
            case None =>
              retrieveOrWaitForContents(name)
      yield activeValue

    private def retrieveOrWaitForContents(
        name: Path
    ) =
      for
        promiseThatMightNotBeUsed <-
          Promise.make[Nothing, FileContents]
        activeUpdates <-
          activeRefresh.updateAndGet {
            activeRefreshes =>
              activeRefreshes.updatedWith(name) {
                case Some(activeUpdate) =>
                  Some(
                    activeUpdate.copy(observers =
                      activeUpdate.observers + 1
                    )
                  )
                case None =>
                  Some(
                    ActiveUpdate(
                      0,
                      promiseThatMightNotBeUsed
                    )
                  )
              }
          }

        activeUpdate = activeUpdates(name)
        finalContents <-
          activeUpdate.observers match
            case 0 =>
              defer {
                printLine(
                  "1st herd member will hit the filesystem"
                ).orDie
                .run
                val contents =
                  fileSystem
                    .readFileExpensive(name)
                    .run
                activeUpdate
                  .promise
                  .succeed(contents)
                  .run
                activeRefresh.update(m =>
                  m - name // Clean out "active" entry
                ).run
                cache.update(m =>
                  m.updated(name, contents) // Update cache
                ).run
                miss.update(_ + 1).run
                contents
            }
            case observerCount =>
              printLine(
                "Slower herd member will wait for response of 1st member"
              ).orDie *> hit.update(_ + 1) *>
                activeUpdate
                  .promise
                  .await
                  .tap(_ =>
                    printLine(
                      "Slower herd member got answer from 1st member"
                    ).orDie
                  )
      yield finalContents

    val hits: ZIO[Any, Nothing, Int] = hit.get

    val misses: ZIO[Any, Nothing, Int] = miss.get

  end Live
end FileService

val users = (0 to 1000).toList.map("User " + _)
//  List("Bill", "Bruce", "James")

val herdBehavior =
  for
    fileService <- ZIO.service[FileService]
    _ <-
      ZIO.foreachParDiscard(users)(user =>
        fileService.retrieveContents(
          Path.of("awesomeMemes")
        )
      )
    _ <- ZIO.debug("=========")
    _ <-
      fileService.retrieveContents(
        Path.of("awesomeMemes")
      )
  yield ()

object ThunderingHerds extends ZIOAppDefault:
  def run =
    herdBehavior
      .provide(FileSystem.live, FileService.live)

trait FileSystem:
  def readFileExpensive(
      name: Path
  ): ZIO[Any, Nothing, FileContents] =
    ZIO
      .succeed(FileSystem.hardcodedFileContents)
      .tap(_ =>
        printLine("Reading from FileSystem")
          .orDie
      )
      .delay(2.seconds)

object FileSystem:
  val hardcodedFileContents =
    FileContents(
      List("viralImage1", "viralImage2")
    )
  val live = ZLayer.succeed(new FileSystem {})
