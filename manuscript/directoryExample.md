## directoryExample

 

### userInputLookup.scala
```scala
 // userInputLookup.scala
package directoryExample

import zio.{UIO, ZIO, ZLayer}
import zio.Console.{readLine, printLine}

import java.io.IOException
import Employee.*
import console.FakeConsole
import processingFunctions.*
import searchFunctions.*

object userInputLookup extends zio.ZIOAppDefault:

  // This example shows the possible modulatriy
  // of scala and FP.
  // The programmer is easily able to make an
  // organized system of functions
  // that can be put in their own files, then
  // imported and used when nessessary.

  def run =
    val logic =
      for
        emps <-
          compileEmployees // Note: Excecutable logic is very concise. The behavior is predefined elsewhere, and only just excecuted in the main.
        _ <-
          printLine(
            "Input full employee name to retrieve from database:   "
          )
        empName <- readLine
        searchedEmp <-
          findEmp(
            empName,
            emps
          ) // look for different employees based on Input Name
        _ <-
          printLine(
            s"Looking for employee... \n" +
              searchedEmp.toString
          )
      yield ()
    (
      for
        console <-
          FakeConsole.withInput(
            "2",
            "96",
            "8"
          ) // Run this program with the following inputs

        _ <-
          logic
            .provide(ZLayer.succeed(console))
            // You can comment out this section
            // if you want to see what the code
            // looks like without
            // catch error handling...
            .catchSome(i =>
              i match
                case e: EmpNotFound =>
                  printLine(
                    "Target employee not in System..."
                  )
            )
            .catchSomeDefect(i =>
              i match
                case e: IOException =>
                  printLine(
                    "Unexpected IOExceptions are the worst..."
                  )
                case e: Throwable =>
                  printLine(
                    s"Huh, wasn't expecting $e"
                  )
            )
      yield ()
    ).exitCode
  end run
end userInputLookup

```


### searchFunctions.scala
```scala
 // searchFunctions.scala
package directoryExample

import zio.ZIO

object searchFunctions:

  case class EmpNotFound(message: String)

  // This function uses recursion to search the
  // list of employees for the given ID.
  // findEmp is a wrapper function for itterate,
  // which is the actual recursive function
  // itterate returns a monad. Either the ID was
  // found, or it wasn't.
  def findEmp(
      ID: Int,
      emps: Vector[Employee]
  ): ZIO[Any, EmpNotFound, Employee] =
    ZIO
      .fromOption(emps.find(_.ID == ID))
      .mapError { case _ =>
        EmpNotFound(
          s"Employee with ID $ID does not exit in the firm directory."
        )
      }

//    def itterate(
//        index: Int,
//        emps: Vector[Employee],
//        targetID: Int
//    ): ZIO[Any, empNotFound, Employee] =
//      if (emps(index).ID == targetID)
//        ZIO.succeed(emps(index))
//      else if (index == 0)
//        ZIO.fail(
//          new empNotFound(
// s"Employee with ID $ID does not exit in the
  // firm directory."
//          )
//        )
//      else
//        itterate(index - 1, emps, targetID)
//    itterate(emps.length - 1, emps, ID)

  def findEmp( // This is an overloaded function. The compiler can identify the correct 'findEmp' function by looking at the parameters used
      name: String,
      emps: Vector[Employee]
  ): ZIO[Any, EmpNotFound, Employee] =
    ZIO
      .fromOption(emps.find(_.getName == name))
      .mapError { case _ =>
        EmpNotFound(
          s"Employee with ID $name does not exit in the firm directory."
        )
      }
end searchFunctions

// def iterate( //Example of tail recursion
// (linear) search
//                 index: Int,
//                 emps: Vector[Employee],
//                 targetName: String
//    ): ZIO[Any, empNotFound, Employee] =
//      if (emps(index).getName == targetName)
//        ZIO.succeed(emps(index))
//      else if (index == 0)
//        ZIO.fail(
//          new empNotFound(
// s"Employee with name $targetName does not
// exit in the firm directory."
//          )
//        )
//      else
//        iterate(index, emps, targetName)
//    iterate(emps.length - 1, emps, name)
//

```


### CSVreader.scala
```scala
 // CSVreader.scala
package directoryExample

import zio.{UIO, ZIO}
import exIOError.errorAtNPerc
import java.io.IOException

def finalizer(
    source: scala.io.Source
) = //Define the finalizer behavior here
  UIO.succeed {
    println("Finalizing: Closing file reader")
    source.close // Close the input source
  }

val readFileContents
    : ZIO[Any, Throwable | IOException, Vector[
      String
    ]] =
  ZIO(
    scala
      .io
      .Source
      .fromFile(
        "src/main/scala/directoryExample/firmData.csv"
      )
  ) // Open the file to read its contents
    .acquireReleaseWith(finalizer) {
      bufferedSource => // Use the bracket method with the finalizer defined above to define behavior on fail.

        val lines =
          for
            line <- bufferedSource.getLines
          yield line
        // This is where you can set the error
        // likely hood
        // This models a fatal IOException
        errorAtNPerc(
          10
        ) // ie, 10 % chance to fail...
        ZIO.succeed(Vector() ++ lines)
    }

```


### employeeDef.scala
```scala
 // employeeDef.scala
package directoryExample

case class Employee(
    ID: Int,
    firstName: String,
    lastName: String,
    department: String
):

  def getName: String =
    val name = s"$firstName $lastName"
    name

  override def toString =
    s"Name: $firstName $lastName. Department: $department. ID: $ID \n"

  def map = this

```


### processingFunctions.scala
```scala
 // processingFunctions.scala
package directoryExample

import zio.ZIO

object processingFunctions:

  // Read a line, and return an employee object
  def linesToEmployees(
      lines: Vector[String]
  ): Vector[Employee] =
    val logic =
      for
        line <- lines
        emp = lineToEmployee(line)
      yield emp
    logic

  def lineToEmployee(line: String): Employee =
    val parts: Array[String] =
      safeSplit(line, ",")
    val emp =
      Employee(
        parts(0).toInt,
        parts(1),
        parts(2),
        parts(3)
      )
    emp

  // This function deals with split()
  // complications with the null safety element
  // of the sbt.
  def safeSplit(line: String, key: String) =
    val nSplit = line.split(key)
    val arr =
      nSplit match
        case null =>
          Array[String]("1", "2", "3")
        case x: Array[String | Null] =>
          x
    arr.collect { case s: String =>
      s
    }

  // Compile list of emp data
  def compileEmployees
      : ZIO[Any, Any, Vector[Employee]] =
    for
      lines <-
        readFileContents.retryN(
          5
        ) // An attempt to open the file occurs 5 times.
      emps = linesToEmployees(lines)
    yield emps
end processingFunctions

```


### directoryExample_full.scala
```scala
 // directoryExample_full.scala
package directoryExample

import exIOError.errorAtNPerc
import zio.{UIO, ZIO}
import zio.Console.{getStrLn, printLine}
import java.io.IOException

object directoryExample_full
    extends zio.ZIOAppDefault:

  // This example will model a database with a
  // list of employees and their information.

  // This example covers a lot of ZIO tools. It
  // covers finalizers, several types of
  // error handling(fatal and non-fatal errors),
  // for comprehensions,
  // functional programming style (ie
  // composition, recursion, pure core/ effectful
  // outside, ect...)

// The fatal error type is a possible
  // IOException. The function errorAtNPerc will
  // trigger
  // and IO exception at the likelihood of n%.
  // The error handling for this is a
  // retry/schedule feature.
  // The non fatal error is for when a search
  // function does not find the target and throws
  // an error.
  // This is handled by a catch block.

  case class Employee(
      ID: Int,
      firstName: String,
      lastName: String,
      department: String
  ):

    def getName: String =
      val name = s"$firstName $lastName"
      name

    override def toString =
      s"Name: $firstName $lastName. Department: $department. ID: $ID \n"

    def map = this

  def finalizer(
      source: scala.io.Source
  ) = // Define the finalizer behavior here
    UIO.succeed {
      println("Finalizing: Closing file reader")
      source.close // Close the input source
    }

  // TODO Wyett: With the current setup, this
  // means that our Github Actions will fail 10%
  // of the time. Consider a
  // FakeRandom that behaves similarly to our
  // FakeConsole. So we could do the 10% failure
  // when running locally, but
  //  have a 100% success rate on Github
  val readFileContents
      : ZIO[Any, Throwable | IOException, Vector[
        String
      ]] =
    ZIO(
      scala
        .io
        .Source
        .fromFile(
          "src/main/scala/directoryExample/firmData.csv"
        )
    ) // Open the file to read its contents
      .acquireReleaseWith(finalizer) {
        bufferedSource => // Use the bracket method with the finalizer defined above to define behavior on fail.

          val lines =
            for
              line <- bufferedSource.getLines
            yield line
          // This is where you can set the error
          // likely hood
          // This models a fatal IOException
          errorAtNPerc(
            100
          ) // ie, 10 % chance to fail...
          ZIO.succeed(Vector() ++ lines)
      }

  // Read a line, and return an employee object
  def linesToEmployees(
      lines: Vector[String]
  ): Vector[Employee] =
    val logic =
      for
        line <- lines
        emp = lineToEmp(line)
      yield emp
    logic

  def lineToEmp(line: String): Employee =
    val parts: Array[String] =
      safeSplit(line, ",")
    val emp =
      Employee(
        parts(0).toInt,
        parts(1),
        parts(2),
        parts(3)
      )
    emp

  // This function deals with split()
  // complications with the null safety element
  // of the sbt.
  def safeSplit(line: String, key: String) =
    val nSplit = line.split(key)
    val arr =
      nSplit match
        case null =>
          Array[String]("1", "2", "3")
        case x: Array[String | Null] =>
          x
    arr.collect { case s: String =>
      s
    }

  // Compile list of emp data
  def compileEmps
      : ZIO[Any, Any, Vector[Employee]] =
    for
      lines <-
        readFileContents.retryN(
          5
        ) // An attempt to open the file occurs 5 times.
      emps = linesToEmployees(lines)
    yield emps

  case class EmpNotFound(message: String)

  // This function uses recursion to search the
  // list of employees for the given ID.
  // findEmp is a wrapper function for iterate,
  // which is the actual recursive function
  // iterate returns a monad. Either the ID was
  // found, or it wasn't.
  def findEmp(
      ID: Int,
      employees: Vector[Employee]
  ): ZIO[Any, EmpNotFound, Employee] =
    def iterate(
        index: Int,
        emps: Vector[Employee],
        targetID: Int
    ): ZIO[Any, EmpNotFound, Employee] =
      if (emps(index).ID == targetID)
        ZIO.succeed(emps(index))
      else if (index == 0)
        ZIO.fail(
          new EmpNotFound(
            s"Employee with ID $ID does not exit in the firm directory."
          )
        )
      else
        iterate(index - 1, emps, targetID)
    iterate(employees.length - 1, employees, ID)
  end findEmp

  def findEmp( // This is an overloaded function. The compiler can identify the correct 'findEmp' function by looking at the parameters used
      name: String,
      employees: Vector[Employee]
  ): ZIO[Any, EmpNotFound, Employee] =
    def iterate( // Example of tail recursion (linear) search
        index: Int,
        employees: Vector[Employee],
        targetName: String
    ): ZIO[Any, EmpNotFound, Employee] =
      if (employees(index).getName == targetName)
        ZIO.succeed(employees(index))
      else if (index == 0)
        ZIO.fail(
          new EmpNotFound(
            s"Employee with name $targetName does not exit in the firm directory."
          )
        )
      else
        iterate(index - 1, employees, targetName)
    iterate(
      employees.length - 1,
      employees,
      name
    )
  end findEmp

// ///////////////////////////////////
  def run =
    val logic =
      for
        employees <-
          compileEmps // Note: Executable logic is very concise. The behavior is predefined elsewhere, and only just executed in the main.
        // _ <- println(emps.toString)
        searchedEmp <-
          findEmp(
            4,
            employees
          ) // look for different employees based on ID
        _ <-
          printLine(
            s"Looking for employee... \n" +
              searchedEmp.toString
          )
      yield ()

    logic
      // You can comment out this section if you
      // want to see what the code looks like
      // without
      // catch error handling...
      .catchSome(i =>
        i match
          case e: EmpNotFound =>
            printLine(
              "Target employee not in System..."
            )
      )
      .catchSomeDefect {
        case e: IOException =>
          printLine(
            "Unexpected IOExceptions are the worst..."
          )
        case e: Throwable =>
          printLine(s"Huh, wasn't expecting $e")
      }
      .exitCode
  end run
end directoryExample_full
// //////////////////////////////////

```


### exIOError.scala
```scala
 // exIOError.scala
package directoryExample

import java.io.IOException
import scala.util.Random

object exIOError:

  // The input needs to be within the range
  // 1-100.
  // This function will randomly throw an io
  // error n % of the time.
  def errorAtNPerc(n: Int) =
    if (n < 0 | n > 100)
      throw new Exception(
        "Invalid Input. Percent Out of Bounds"
      )
    else
      val rand = Random.nextInt(101)
      if ((0 to n).contains(rand))
        throw new IOException(
          "An unexpected IOException Occured!!!"
        )

```

