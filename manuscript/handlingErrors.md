## handlingErrors

 

### experiments/src/main/scala/handlingErrors/catching.scala
```scala
package handlingErrors

import zio.*
import zio.Console.*
import handlingErrors.file
import java.io.IOException

def standIn: ZIO[Console, IOException, Unit] =
  printLine("Im a stand-in")

object catching extends zio.ZIOAppDefault:

  val logic = loadFile("TargetFile")

  def run =
    logic
      .catchAll(_ =>
        println("Error Caught")
        loadBackupFile()
      )
      .exitCode

// standIn.exitCode

```


### experiments/src/main/scala/handlingErrors/fallback.scala
```scala
package handlingErrors

import zio.*
import zio.Console
import scala.util.Random
// A useful way of dealing with errors is by
// using the
// `orElse()` method.

case class file(name: String)

def loadFile(fileName: String) =
  if (Random.nextBoolean())
    println("First Attempt Successful")
    ZIO.succeed(file(fileName))
  else
    println("First Attemp Not Successful")
    ZIO.fail("File not found")

def loadBackupFile() =
  println("Backup file used")
  ZIO.succeed(file("BackupFile"))

object fallback extends zio.ZIOAppDefault:

  // orElse is a combinator that can be used to
  // handle
  // effects that can fail.

  def run =
    val loadedFile: UIO[file] =
      loadFile("TargetFile")
        .orElse(loadBackupFile())
    loadedFile.exitCode

```


### experiments/src/main/scala/handlingErrors/folding.scala
```scala
package handlingErrors

import zio.*
import zio.Console.*
import handlingErrors.file
import handlingErrors.standIn

object folding extends ZIOAppDefault:
// When applied to ZIO, fold() allows the
  // programmer to handle both failure
// and success at the same time.
// ZIO's fold method can be broken into two
  // pieces: fold(), and foldM()
// fold() supplied a non-effectful handler, why
  // foldM() applies an effectful handler.

  val logic = loadFile("targetFile")

  def run =
    val message =
      logic.fold(
        _ => "The file wouldn't open... ",
        _ => "The file opened!"
      ) // Non-effectful handling

    logic
      .foldZIO(
        _ => loadBackupFile(),
        _ =>
          printLine(
            "The file opened on first attempt!"
          )
      ) // Effectful handling
      .exitCode
end folding

```


### experiments/src/main/scala/handlingErrors/value.scala
```scala
package handlingErrors

import zio.*

object value:
  // Either and Absolve take ZIO types and
  // 'surface' or 'submerge'
  // the error.

  // Either takes an ZIO[R, E, A] and produces an
  // ZIO[R, Nothing, Either[E,A]]
  // The error is 'surfaced' by making a
  // non-failing ZIO that returns an Either.

  // Absolve takes an ZIO[R, Nothing,
  // Either[E,A]], and returns a ZIO[R,E,A]
  // The error is 'submerged', as it is pushed
  // from an either into a ZIO.

  val zEither: UIO[Either[String, Int]] =
    IO.fail("Boom").either

  // IO.fail("Boom") is naturally type
  // ZIO[R,String,Int], but is
  // converted into type UIO[Either[String, Int]

  def sqrt(
      input: UIO[Double]
  ): IO[String, Double] =
    ZIO.absolve(
      input.map(value =>
        if (value < 0.0)
          Left("Value must be >= 0.0")
        else
          Right(Math.sqrt(value))
      )
    )
end value

// The Left-Right statements naturally from an
// 'either' of type either[String, Double].
// the ZIO.absolve changes the either into an
// ZIO of type IO[String, Double]

```

