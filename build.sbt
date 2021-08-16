import java.io.File
import java.nio.file.{Files, Path}

enablePlugins(MdocPlugin)

name := "EffectOrientedProgramming"

scalaVersion := "3.0.2-RC1"

scalacOptions += "-Yexplicit-nulls"
scalacOptions -= "-explain-types"
scalacOptions -= "-explain"
scalacOptions -= "-encoding"

val zioVersion = "2.0.0-M1"

libraryDependencies ++=
  Seq(
    "org.jetbrains" % "annotations-java5" %
      "22.0.0",
    "org.scalameta"      %
      "scalafmt-dynamic" % "3.0.0-RC7" cross
      CrossVersion.for3Use2_13,
    "dev.zio"     %% "zio"    % zioVersion,
    "com.typesafe" % "config" % "1.4.1",
    //     cross CrossVersion.for3Use2_13,
    "dev.zio" %% "zio-test"     % zioVersion,
    "dev.zio" %% "zio-test-sbt" % zioVersion %
      Test,
    "org.scalameta" %% "munit" % "0.7.28" % Test,
    "io.circe"  % "circe-core_3"  % "0.15.0-M1",
    "io.circe" %% "circe-generic" % "0.15.0-M1",
    "com.softwaremill.sttp.client3" %% "circe" %
      "3.3.13",
    "com.softwaremill.sttp.client3" %% "core" %
      "3.3.13"
// "io.d11" %% "zhttp" % "1.0.0.0-RC17", //
    // TODO Check for updates supporting ZIO2
    // milestones
// "io.d11" %% "zhttp-test" % "1.0.0.0-RC17"
    // % Test,
//    "dev.zio" %% "zio-json" % "0.2.0-M1"
  )

testFrameworks +=
  new TestFramework(
    "zio.test.sbt.ZTestFramework"
  )

testFrameworks +=
  new TestFramework("munit.Framework")

mdocIn := file("Chapters")

mdocOut := file("manuscript")

scalafmtOnCompile := true

// chapter files are ordered: 01_foo.md
// if 01+_bar.md is created, it becomes 02_bar.md
// and 02_baz.md becomes 03_baz.md

lazy val orderChapters = taskKey[Unit]("Order the Chapters")

orderChapters := {

  import scala.io.Source
  import scala.collection.JavaConverters._

  val chapters = mdocIn.value / "00_chapters"

  val source = Source.fromFile(chapters)

  source.getLines().zipWithIndex.foreach { case (name, num) =>
    val n: String = if (num < 10) s"0$num" else num.toString
    val outF = mdocIn.value / s"${n}_$name.md"
    val maybeInF = Files.list(mdocIn.value.toPath).iterator().asScala.find { f =>
      f.toFile.getName.endsWith(name + ".md")
    }

    maybeInF.foreach { inF =>
      Files.move(inF, outF.toPath)
    }
  }

  source.close()

}

Compile / compile := (Compile / compile).dependsOn(orderChapters).value


lazy val bookTxt = taskKey[Unit]("Create the Book.txt")

bookTxt := {

  import scala.util.Try
  import scala.collection.JavaConverters._

  val files = Files.list(mdocIn.value.toPath).iterator().asScala

  def chapterNum(path: Path): Option[Int] = {
    val justFile = path.toFile.getName.split(File.pathSeparator).last

    justFile.split('_').headOption.flatMap { firstPart =>
      Try(firstPart.toInt).toOption
    }
  }

  val chapters = files.flatMap { f =>
    if (f.toFile.ext == "md") {
      chapterNum(f).map(_ -> f)
    }
    else {
      None
    }
  }.toSeq.sortBy(_._1).map(_._2.toFile.getName.stripPrefix(mdocIn.value.getName).stripPrefix(File.pathSeparator))

  val bookTxtPath = mdocOut.value / "Book.txt"
  Files.write(bookTxtPath.toPath, chapters.asJava)
}

mdoc := mdoc.dependsOn(bookTxt).evaluated