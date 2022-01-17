## scalaBasics

 

### map.scala
```scala
 // map.scala
package scalaBasics

object map:

  val nums    = Vector(0, 2, 1, 4, 3)
  val letters = Vector('a', 'b', 'c', 'd', 'e')

  @main
  def mapEx =
    val combined =
      nums.map(letters) // re-order letters
    println(combined)

    val comb2 =
      nums.map(i =>
        i -> letters(i)
      ) // assign values of nums to letters
    println(comb2)

```


### fpOption.scala
```scala
 // fpOption.scala
// package scalaBasics

// This implementation of the 'Option Type' shows
// some of the FP style,
// and use of higher order functions.
/* class fpOption[+A] :
 * def map[B](f: A => B): Option[B] =
 * this match { case None => None case Some(a) =>
 * Some(f(a)) }
 *
 * def flatMap[B](f: A => Option[B]): Option[B] =
 * map(f) getOrElse None
 *
 * def getOrElse[B >: A](default: => B): B =
 * this match { case None => default case Some(a)
 * => a }
 *
 * def orElse[B >: A](ob: => Option[B]):
 * Option[B] =
 * this map (Some(_)) getOrElse ob
 *
 * def filter(f: A => Boolean): Option[A] =
 * flatMap( a => if (f(a)) Some(a) else None) */

```


### fold.scala
```scala
 // fold.scala
package scalaBasics

object fold:

  // This set of examples explains how the fold
  // function works, and how to use it.

  // There are three types of folds: fold(),
  // foldLeft, foldRight
  @main
  def foldEx1 =
    // All three take two arguments:
    val listEx = List(1, 2, 3, 4, 5, 6)
    // The first argument is where to start from,
    // the second argument is a function that
    // takes two parameters.
    // The first parameter is the accumulated or
    // 'folded up' variable, and the second is
    // the
    // next thing to be folded into the first
    // variable.
    val folded =
      listEx.fold(0)((z, i) =>
        println(s"z = $z")
        println(s"i = $i \n")
        z + i
      )
    // The first parameter, 0, is where the
    // folding will start from.
    // The second variable is a function taking
    // z, and i. z being the previously
    // accumulated
    // variable, and i being the next variable to
    // be accumulated.
    println(folded)
  end foldEx1

  @main
  def foldEx2 =
    // foldLeft() and foldRight() fold from
    // specific directions.
    // foldLeft() folds FROM the left.
    // foldRight() folds FROM the right.
    val letterList =
      List('a', 'b', 'c', 'd', 'e')
    println("Folding from left: ")
    val leftFolded =
      letterList.foldLeft("")((z, i) =>
        println(s"z = $z")
        println(s"i = $i \n")
        s"$z${i.toString} "
      )
    println(s"leftFolded = $leftFolded \n")

    println("Folding from right: ")
    val rightFolded =
      letterList.foldRight("")((i, z) =>
        println(s"z = $z")
        println(s"i = $i  \n")
        s"$z${i.toString}"
      )

    // NOTE: The order of the parameters in the
    // passed in function are significant
    // between fold left and fold right.
    // With fold left, the left variable (A, b)
    // is the accumulated variable.
    // With fold right, the right variable ( a,
    // B) is the accumulated variable.
    // Thus, the accumulated var 'z' is on the
    // right side of the parameters for the
    // passed in function.

    println(s"rightFolded = $rightFolded")
  end foldEx2
end fold

@main
def foldEx3 =
  // Folding is not always just accumulating in
  // some way. It can also just
  // be used to iterate through a List. Here is
  // an example of using a foldLeft
  // to find the max value in a list.

  val numList = List(1, 4, 2, 10, 6, 3, 7, 9)

  val maxOfList =
    numList.foldLeft(numList(0))((z, i) =>
      println(s"z = $z")
      println(s"i = $i \n")
      if (i > z)
        i
      else
        z
    )
  // In this case, the 'accumulated' var is z,
  // but instead of building up through the
  // values,\
  // in the passed in function, z is compared to
  // i, and which ever is greater is passed on as
  // z.

  println(maxOfList)
end foldEx3

@main
def foldEx4 =
  // Variable type also becomes important when
  // folding.
  // The accumulated variable must be of the nw
  // type, and the
  // variable to be added is of the old type.
  val numList = List(1, 2, 3, 4, 5, 6, 7)

  val strVersion =
    numList.foldLeft("": String)(
      (z: String, i: Int) => s"$z ${i.toString}"
    )

  println(strVersion)

@main
def foldEx5 =
  // Folding can be a good way to make a list in
  // functional Programming.

  val numList = List(1, 2, 3, 4, 5, 6, 7)

  val strList =
    numList.foldLeft(List.empty[String])(
      (z: List[String], i: Int) =>
        i.toString :: z
    )

  println(strList)

```


### flatMap.scala
```scala
 // flatMap.scala
package scalaBasics

import scala.util.*

object flatMap {
  // In its most basic sense, flatMap is the
  // combination of
  // the two functions map(), and flatten().

  /* @main def flatMapEx =
   * val nums =
   * Seq(List(1,2,3),List(1,2,3),List(1,2,3))
   *
   * def addOne(num:Int): Int =
   * num + 1
   *
   * println("\nFlat, then map: ") val flatWords
   * = nums.flatten println(flatWords) val
   * mappedFlatWords = flatWords.map(addOne)
   * println(mappedFlatWords)
   *
   * println("\nflatMap: ") val flatMapped =
   * nums.flatMap(addOne) println(flatMapped) */
  /* //In Functional Programming, flatMap can be
   * used in error handling.
   * // Flat Map can behave like a Map() that can
   * fail.
   * //For example, when using Options:
   *
   * def map[B](f: A => B):Option[B] =
   * this match { case None => None //If the
   * object calling map is None, return None case
   * Some(a) => Some(f(a)) //If the object
   * calling map is something, return something
   * holding f(something) } //Map calls f for
   * each of it's item, then returns an Option
   * for the whole list
   *
   * def flatMap[B](f: A => Option[B]):Option[B]
   * this match { case None => None //If the
   * object calling map is None, return None case
   * Some(a) => f(a) //If the object calling map
   * is something, return f(something) }
   * //flatMap calls a function f that returns an
   * Option //for each of the items. Then it
   * returns the transformed list as an option. */

}

```


### usingMains.scala
```scala
 // usingMains.scala
package scalaBasics

// This example will go over how to use the @main
// notation

object usingMains:

  // Within a package, the compiler will analyze
  // all of the executable
  // functions, and select one of them as the
  // 'main function'.
  // Something useful that scala 3 does is to
  // give the programmer the
  // ability to define multiple main functions
  // even within the same file.

  @main
  def main1() = println("I am main function 1!")

  @main
  def main2() = println("I am main function 2!")

  // If a function has the @main tag before its
  // definition,
  // sbt will recognize it as a main function,
  // and it will be seen
  // as runnable.

  // This is a great way to run specific
  // functions, or combination of functions.

  def foo(input: String) = print(input)

  def bar = print("llo")

  def f = println(" there!")

  @main
  def message =
    val str = "He"
    foo(str)
    bar
    f
end usingMains

```


### tailEndRecursion.scala
```scala
 // tailEndRecursion.scala
package scalaBasics

object tailEndRecursion:

  // The Scala compiler will be able to optimize
  // a recursive structure into byte code similar
  // to a while loop if the recursive structure
  // is a 'tail end' recursion.

  def tailEndEx(num: Int): Int =
    @annotation.tailrec
    def fib(n: Int, a: Int, b: Int): Int =
      if (n == 0)
        a
      else if (n == 1)
        b
      else
        fib(n - 1, b, a + b)
    fib(num, 0, 1)

  @main
  def fib6 =
    val fib6 = tailEndEx(6) // Expected output: 8
    println(fib6)
end tailEndRecursion

```


### forComprehension.scala
```scala
 // forComprehension.scala
package scalaBasics

object forComprehension:

  // This example goes through the basics of the
  // for comprehension.
  // Initially, it will be compared to the for
  // loop equivalent.

  val numbers = Vector(1, 2, 3, 4, 5, 6)

  // for loop
  def forLoopEx =
    println("For Loop: ")
    for (i <- Range(0, 6)) {
      val value = numbers(i)
      print(s"$value, ")
    }
    println()

  // for comprehension
  def forCompEx =
    println("For Comprehension: ")
    for {
      i <- numbers
    } print(s"$i" + ", ")
    println()

  // Example function: This function is slightly
  // more complex.
  // It demonstrates how to filter elements in a
  // for comprehension.
  def evenGT5v1(v: Vector[Int]): Vector[Int] =
    // 'var' so we can reassign 'result':
    println(
      "Finding values greater than 5 and even: "
    )
    var result = Vector[Int]()
    for {
      n <-
        v // Take the input value v, and iterate through each element
      if n > 5 // If n is greater than 5
      if n % 2 == 0 // and n is divisible by 2
    } result =
      result :+
        n  // Then add n to the result list
    result // return result

// To remove the use of a var, and simplify the
  // code, you can use the yield keyword.
  // Yielding will create a list of all the
  // values that satisfied the criteria.
  // Yielding essentially creates the list in
  // place.
  def evenGT5v2(v: Vector[Int]): Vector[Int] =
    // 'var' so we can reassign 'result':
    println(
      "Finding values greater than 5 and even: "
    )
    for
      n <-
        v // Take the input value v, and iterate through each element
      if n > 5      // If n is greater than 5
      if n % 2 == 0 // and n is divisible by 2
    yield n // create a list of the values of n.

  @main
  def run() =
    forLoopEx
    forCompEx

    val v =
      Vector(1, 2, 3, 5, 6, 7, 8, 10, 13, 14, 17)
    println(evenGT5v1(v))
    println(evenGT5v2(v))

  // For comprehensions can also be used to
  // string together multiple events.
  // In some cases, this is called changing. A
  // programmer would use a for comprehension
  // as it more clearly shows the sequential
  // nature
  // of a chain.

  // At this level, the arrow '<-" is called the
  // generator. The generator acts as either a
  // flatmap function,
  // or a map function. When using the 'yield'
  // functionality, the last '<-' represents a
  // map function instead of a
  // flatmap.

  // Here is a side by side example of a series
  // of function when called inside a for
  // comprehension vs not in a for comprehension:

  case class color(name: String):

    def flatMap(f: String => color): color =
      f(this.name)

    def map(f: String => String): color =
      color(f(this.name))

  object color:

    def makeRed: color = new color("red")

    def changeColor(name2: String): color =
      new color(name2)

    def changeColor2(name2: String): color =
      new color(name2)

// //////////////////////////////////////////////////////
  @main
  def thefor =
    val colorChanges =
      for
        color1 <- color.makeRed
        color2 <- color.changeColor("green")
        color3 <- color.changeColor2("yellow")
      yield color3
    println(colorChanges)

  @main
  def unraveled =
    val colorChanges =
      color
        .makeRed
        .flatMap {
          color1 => // color1 is a string
            color
              .changeColor("green")
              .flatMap {
                color2 => // color2 is a string
                  color
                    .changeColor2("yellow")
                    .map {
                      color3 => // color3 is a color
                        color3
                    }
              }
        }
    println(colorChanges)
  end unraveled
end forComprehension

/* kat2 <- again(kat1) kat3 <- again(kat2) yield
 * kat3 // what we return here has to be a
 * Contents because it must fit in the Box */

// println(colorChanges)

/* @main def unravelled =
 * val kats = Box.observe.flatMap { kat1 =>
 * again(kat1).flatMap { kat2 => again(kat2).map
 * { kat3 => kat3 } } } */

```

