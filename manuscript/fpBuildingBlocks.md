## fpBuildingBlocks

 

### experiments/src/main/scala/fpBuildingBlocks/exIOError.scala
```scala
package fpBuildingBlocks

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
          "An unexpected IOException Occurred!!!"
        )

```


### experiments/src/main/scala/fpBuildingBlocks/generalFunctions.scala
```scala
package fpBuildingBlocks

// The purpose of this example is to show the
// reader how general functions work.
// How a function can be passed in a type as a
// parameter, and how it may effect the
// behavior of the function.
object generalFunctions {

  // TODO:This is commented out because the scala
  // formatter doesn't like it...

  /* //Most common example of a general function:
   * Lists
   *
   * val intList = List[Int](1,2,3,4) //The Int
   * variable type is passed into the List
   * definition as a type parameter
   *
   * val stringList =
   * List[String]("1","2","3","4") //Likewise
   * with the String type.
   *
   * //A type is usually indicated through the
   * use of square brackets. The contents usually
   * use parens
   *
   * //This example displays how a function can
   * be defined without a specific type.
   * //The different types can be passed into the
   * function, and the function will behave
   * //differently depending on the type that was
   * passed in.
   * case class stringAdd(num1:String,
   * num2:String) case class intAdd(num1:Int,
   * num2:Int)
   *
   * def add(x:B) =
   * x match { case x:stringAdd => x.num1.toInt +
   * x.num2.toInt case x:intAdd => x.num1 +
   * x.num2 case x:_ => println("What is this?")
   * }
   *
   * @main def additionEx =
   * val sAdd = stringAdd("1", "2") val iAdd =
   * intAdd(1,2)
   *
   * val sAdded = add[stringAdd](sAdd) //The type
   * of the object is passed in as a param val
   * iAdded = add[intAdd](iAdd)
   *
   * println(sAdded) println(iAdded) */
}

```


### experiments/src/main/scala/fpBuildingBlocks/higherOrderFunctions.scala
```scala
package fpBuildingBlocks

// Higher Order functions are functions that
// accept other functions as
// parameters.

object higherOrderFunctions:

  def foo1: Unit = // [f: Unit => Unit]
    println("I am function 1!!!")

  def foo2(x: Int): Unit = // [f: Int => Unit]
    println(s"I was given $x!!!")

  def add(
      x: Int,
      y: Int
  ): Int = // [f: (Int, Int) => Int]
    val added = x + y
    println(s"$x + $y = $added!!!")
    added

  def sub(
      x: Int,
      y: Int
  ): Int = // [f: (Int, Int) => Int]
    val subtracted = x - y
    println(s"$x - $y = $subtracted!!!")
    subtracted

  // The parameters of higherOrder are a function
  // f, that takes 2 Int,
  // and returns an Int, Int x, and Int y
  def higherOrder(
      f: (Int, Int) => Int,
      x: Int,
      y: Int
  ) =
    val mathed = f(x, y)
    println(
      s"I was given a function, Int $x and Int $y. \nThe output is $mathed"
    )
    mathed

  @main
  def higherOrders =
    val add3n2 =
      higherOrder(
        add,
        3,
        2
      ) // Here, we are passing in a function as a parameter
    println("\n")
    val sub3n2 = higherOrder(sub, 3, 2)
end higherOrderFunctions

// There are several higher order functions you
// probably already use!
// foreach(), map(), and flatMap() all take in
// functions as parameters.

```


### experiments/src/main/scala/fpBuildingBlocks/pureCore.scala
```scala
package fpBuildingBlocks

import scala.math.BigDecimal
import exIOError.errorAtNPerc
import java.io.IOException

object pureCore:
  // Here, we will be using Options as a way to
  // move effects out of a
  // pure functional core, and into an effectful
  // outside.
  // errorAtNPerc will model our example effect.

  // Non-Pure
  // (The function models a translation with
  // cash)
  def transaction(
      cashPayment: Double,
      price: Double
  ): Double =
    errorAtNPerc(
      50
    ) // There will be a 50% chance of random failure to model an effect
    BigDecimal(cashPayment - price)
      .setScale(
        2,
        BigDecimal.RoundingMode.HALF_UP
      )
      .toDouble

  def statement(valid: Boolean): Unit =
    if (valid)
      println("Have a wonderful Day!")
    else
      println(
        "I'm very sorry, there was an error with our system..."
      )

  // This string of logic is considered impure.
  // The programmer is checking for issues
  // throughout the logic.
  @main
  def NonPure =
    val change =
      try
        transaction(20, 19.99)
      catch
        case e: IOException =>
          None

    val continue =
      change match
        case None =>
          println(
            "An Error occurred in the Transaction"
          );
          false
        case _ =>
          println(s"Your change is $change");
          true

    statement(continue)
  end NonPure
end pureCore

/* //Pure Form def
 * transaction2(cashPayment:Double,
 * price:Double):Option[Double] =
 * errorAtNPerc(50)//There will be a 50% chance
 * of random failure to model an effect
 * BigDecimal(cashPayment - price).setScale(2,
 * BigDecimal.RoundingMode.HALF_UP).toDouble
 *
 * def statement2(valid:Boolean):Unit =
 * if (valid) println("Have a wonderful Day!")
 * else println("I'm very sorry, there was an
 * error with our system...") */

```

