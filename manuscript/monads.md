## monads

 

### Solution1.scala
```scala
 // Solution1.scala
// Monads/Solution1.scala
package monads

def eshow(n: Char) =
  println(s">> show($n) <<")

  def op(id: Char, msg: String) =
    val result =
      if n == id then
        Left(msg + id.toString)
      else
        Right(msg + id.toString)
    println(s"op($id): $result")
    result

  val compose =
    for
      a: String <- op('a', "")
      b: String <- op('b', a)
      c: String <- op('c', b)
    yield
      println(s"Completed: $c")
      c

  println(compose);
  for (failure <- compose.left)
    println(s"Error-handling for $failure")

end eshow

@main
def eresults = 'a' to 'd' foreach eshow

```


### Diapers.scala
```scala
 // Diapers.scala
package monads

import scala.util.Random

enum Diaper:

  def flatMap(f: String => Diaper): Diaper =
    this match
      case _: Empty =>
        this
      case s: Soiled =>
        f(
          s.description
        ) // written a different way for illustrating the different syntax options

  def map(f: String => String): Diaper =
    this match
      case _: Empty =>
        this
      case Soiled(description) =>
        Soiled(f(description))

  // optionally we can build this on top of
  // flatMap

  // flatMap(f.andThen(Soiled.apply))

  /* flatMap { description =>
   * Soiled(f(description)) } */

  case Empty()
  case Soiled(description: String)
end Diaper

def look: Diaper =
  val diaper =
    if (Random.nextBoolean())
      Diaper.Empty()
    else
      Diaper.Soiled("Ewwww")

  println(diaper)

  diaper

def change(description: String): Diaper =
  println("changing diaper")
  Diaper.Empty()

@main
def baby =
  val diaper: Diaper =
    for
      soiled <-
        look // When this returns Diaper.empty, we fall out and don't get to the left side
      freshy <-
        change(
          soiled
        ) // When this returns Diaper.empty, we fall out and don't get to the left side
    yield throw new RuntimeException(
      "This will never happen."
    ) // TODO Alter example so we don't have a pointless yield

  println(diaper)

```


### Jackbot.scala
```scala
 // Jackbot.scala
package monads
// Scratch/experimental example.

object Jackbot: // Keep from polluting the 'monads' namespace
  type Result[F, S]  = Either[F, S]
  type Fail[F, S]    = Left[F, S]
  type Succeed[F, S] = Right[F, S]
  object Fail:
    def apply[F](value: F) = Left.apply(value)
    def unapply[F, Any](fail: Fail[F, Any]) =
      Left.unapply(fail)
  object Succeed:
    def apply[S](value: S) = Right.apply(value)
    def unapply[Any, S](
        succeed: Succeed[Any, S]
    ) = Right.unapply(succeed)

@main
def jackbotWroteThis =
  val ok: Jackbot.Result[Int, String] =
    Jackbot.Succeed("Hello")
  val notOk: Jackbot.Result[Int, String] =
    Jackbot.Fail(4)
  val result1 =
    ok match
      case Jackbot.Succeed(value: String) =>
        value
      case Jackbot.Fail(code: Int) =>
        s"Failed with code ${code}"
      case _ =>
        s"Exhaustive fix"
  println(result1)
  val result2 =
    notOk match
      case Jackbot.Succeed(value: String) =>
        value
      case Jackbot.Fail(code: Int) =>
        s"Failed with code ${code}"
      case _ =>
        s"Exhaustive fix"
  println(result2)
end jackbotWroteThis

/* Output:
 * Hello Failed with code 4 */

```


### Solution4b.scala
```scala
 // Solution4b.scala
// Monads/Solution4b.scala
package monads
import ResultEnum.*

def showRE(n: Char) =
  def op(id: Char, msg: String): ResultEnum =
    val result =
      if n == id then
        FailRE(msg + id.toString)
      else
        SuccessRE(msg + id.toString)
    println(s"op($id): $result")
    result

  val compose: ResultEnum =
    for
      a: String <- op('a', "")
      b: String <- op('b', a)
      c: String <- op('c', b)
    yield
      println(s"Completed: $c")
      c.toUpperCase.nn

  println(compose)
  compose match
    case FailRE(why) =>
      println(s"Error-handling for $why")
    case SuccessRE(data) =>
      println("Success: " + data)

end showRE

@main
def resultsRE = 'a' to 'd' foreach showRE

```


### Result.scala
```scala
 // Result.scala
// Monads/Result.scala
package monads

trait Result:
  def flatMap(f: String => Result): Result =
    println(s"flatMap() on $this")
    this.match
      case fail: Fail =>
        fail
      case Success(c) =>
        f(c)

  def map(f: String => String): Result =
    println(s"map() on $this")
    this.match
      case fail: Fail =>
        fail
      case Success(c) =>
        Success(f(c))

case class Fail(why: String)     extends Result
case class Success(data: String) extends Result

```


### ShowGenericResult.scala
```scala
 // ShowGenericResult.scala
// Monads/ShowGenericResult.scala
// Exercise solution to "Verify
// GenericResult.scala works"
package monads

def gshow(n: Char) =
  println(s">> show($n) <<")

  def op(id: Char, msg: String) =
    val result =
      if n == id then
        GFail(msg + id.toString)
      else
        GSuccess(msg + id.toString)
    println(s"op($id): $result")
    result

  val compose =
    for
      a: String <- op('a', "")
      b: String <- op('b', a)
      c: String <- op('c', b)
    yield
      println(s"Completed: $c")
      c

  if compose.isInstanceOf[GFail[String]] then
    println(s"Error-handling for $compose")
end gshow

@main
def gresults = 'a' to 'd' map gshow

```


### Solution5.scala
```scala
 // Solution5.scala
package monads

val sol5a =
  for
    a <- Some("A")
    b <- Some("B")
    c <- Some("C")
  yield s"Result: $a $b $c"

val sol5b =
  Some("A").flatMap(a =>
    Some("B").flatMap(b =>
      Some("C").map(c => s"Result: $a $b $c")
    )
  )

@main
def sol5 =
  println(sol5a)
  println(sol5b)

```


### GenericResult.scala
```scala
 // GenericResult.scala
// Monads/GenericResult.scala
package monads

trait GResult[+W, +D]:
  def flatMap[W1, B](
      f: D => GResult[W1, B]
  ): GResult[W | W1, B] =
    println(s"flatMap() on $this")
    this.match
      case GSuccess(c) =>
        f(c)
      case fail: GFail[W] =>
        fail

  def map[B](f: D => B): GResult[W, B] =
    println(s"map() on $this")
    this.match
      case GSuccess(c) =>
        GSuccess(f(c))
      case fail: GFail[W] =>
        fail
end GResult

case class GFail[+W](why: W)
    extends GResult[W, Nothing]
case class GSuccess[+D](data: D)
    extends GResult[Nothing, D]

```


### Operation.scala
```scala
 // Operation.scala
package monads

enum Operation:

  def flatMap(
      f: String => Operation
  ): Operation =
    this match
      case _: Bad =>
        this
      case s: Good =>
        f(s.content)

  def map(f: String => String): Operation =
    this match
      case _: Bad =>
        this
      case Good(content) =>
        Good(f(content))

  case Bad(reason: String)
  case Good(content: String)
end Operation

def httpOperation(
    content: String,
    result: String
): Operation =
  if (content.contains("DbResult=Expected"))
    Operation
      .Good(content + s" + HttpResult=$result")
  else
    Operation
      .Bad("Did not have required data from DB")

def businessLogic(
    content: String,
    result: String
): Operation =
  if (content.contains("HttpResult=Expected"))
    Operation
      .Good(content + s" + LogicResult=$result")
  else
    Operation.Bad(
      "Did not have required data from Http Call"
    )

@main
def happyPath =
  println(
    for
      dbContent <-
        Operation.Good("DbResult=Expected")
      httpContent <-
        httpOperation(dbContent, "Expected")
      logicContent <-
        businessLogic(httpContent, "Expected")
    yield logicContent
  )

@main
def sadPathDb =
  println(
    for
      dbContent <-
        Operation.Good("DbResult=Unexpected")
      httpContent <-
        httpOperation(dbContent, "Expected")
      logicContent <-
        businessLogic(httpContent, "Expected")
    yield logicContent
  )

@main
def sadPathHttp =
  println(
    for
      dbContent <-
        Operation.Good("DbResult=Expected")
      httpContent <-
        httpOperation(dbContent, "Unexpected")
      logicContent <-
        businessLogic(httpContent, "Expected")
    yield logicContent
  )

@main
def failAfterSpecifiedNumber =
  def operationConstructor(
      x: Int,
      limit: Int
  ): Operation =
    if (x < limit)
      Operation.Good(s"Finished step $x")
    else
      Operation.Bad("Failed after max number")

  def badAfterXInvocations(x: Int): Operation =
    for
      result1 <- operationConstructor(0, x)
      result2 <- operationConstructor(1, x)
      result3 <- operationConstructor(2, x)
      result4 <- operationConstructor(3, x)
    yield result4

  println(badAfterXInvocations(5))
end failAfterSpecifiedNumber

```


### Solution3.scala
```scala
 // Solution3.scala
// Monads/Solution3.scala
package monads

def oshow(n: Char) =
  println(s">> show($n) <<")

  def op(id: Char, msg: String) =
    val result =
      if n == id then
        None
      else
        Some(msg + id.toString)
    println(s"op($id): $result")
    result

  val compose =
    for
      a: String <- op('a', "")
      b: String <- op('b', a)
      c: String <- op('c', b)
    yield
      println(s"Completed: $c")
      c

  println(compose);
  if compose == None then
    println(s"Error-handling for None")

end oshow

@main
def oresults = 'a' to 'd' foreach oshow

```


### ShowResult.scala
```scala
 // ShowResult.scala
// Monads/ShowResult.scala
package monads

def show(n: Char) =
  def op(id: Char, msg: String): Result =
    val result =
      if n == id then
        Fail(msg + id.toString)
      else
        Success(msg + id.toString)
    println(s"$n => op($id): $result")
    result

  val compose: Result =
    for
      a: String <- op('a', "")
      b: String <- op('b', a)
      c: String <- op('c', b)
    yield
      println(s"Yielding: $c + 'd'")
      c + 'd'

  println(s"compose: $compose")
  compose match
    case Fail(why) =>
      println(s"Error-handling for $why")
    case Success(data) =>
      println("Successful case: " + data)

end show

@main
def results = 'a' to 'd' foreach show

```


### TypeConstructor.scala
```scala
 // TypeConstructor.scala
package monads
// Just trying to understand what a type
// constructor is

// trait TypeConstructor[F[_], A]:
//  def use(tc: F): F[A]

trait Process[F[_], O]:
  def runLog(implicit
      F: MonadCatch[F]
  ): F[IndexedSeq[O]]

trait MonadCatch[F[_]]:
  def attempt[A](
      a: F[A]
  ): F[Either[Throwable, A]]
  def fail[A](t: Throwable): F[A]

```


### DesugaredComprehension.scala
```scala
 // DesugaredComprehension.scala
package monads

val fc1 =
  for
    a <- Right("A")
    b <- Right("B")
    c <- Right("C")
  yield s"Result: $a $b $c"

val fc2 =
  Right("A").flatMap(a =>
    Right("B").flatMap(b =>
      Right("C").map(c => s"Result: $a $b $c")
    )
  )

@main
def expanded =
  println(fc1)
  println(fc2)

```


### Flattening.scala
```scala
 // Flattening.scala
package monads

@main
def hmmm() =

  val abc = List("a", "bb", "ccc")
  println(abc.flatten)
  val abbccc =
    abc.flatMap { s =>
      s.toCharArray match
        case a: Array[Char] =>
          a.toList
        case null =>
          List.empty
    }
  println(abbccc)

  // We need to provide an instance of a function
  // that can
  // transform an Int to a IterableOnce[B]
  implicit def intToIterable(
      i: Int
  ): IterableOnce[Int] = List.fill(i)(i)

  val oneAndFiveFives = List(1, 5)
  println(oneAndFiveFives.flatten)
end hmmm

```


### Solution2.scala
```scala
 // Solution2.scala
// Monads/Solution2.scala
package monads

def ishow(n: Char) =
  println(s">> show($n) <<")

  def op(id: Char, i: Int) =
    val result =
      if n == id then
        Left(i + id)
      else
        Right(i + id)
    println(s"op($id): $result")
    result

  val compose =
    for
      a: Int <- op('a', 0)
      b: Int <- op('b', a)
      c: Int <- op('c', b)
    yield
      println(s"Completed: $c")
      c

  println(compose);
  for (failure <- compose.left)
    println(s"Error-handling for $failure")

end ishow

@main
def iresults = 'a' to 'd' foreach ishow

```


### Solution4a.scala
```scala
 // Solution4a.scala
// Monads/Solution4a.scala
package monads

enum ResultEnum:
  def flatMap(
      f: String => ResultEnum
  ): ResultEnum =
    println(s"flatMap() on $this")
    this.match
      case SuccessRE(c) =>
        f(c)
      case fail: FailRE =>
        fail

  def map(f: String => String): ResultEnum =
    println(s"map() on $this")
    this.match
      case SuccessRE(c) =>
        SuccessRE(f(c))
      case fail: FailRE =>
        fail

  case FailRE(why: String)
  case SuccessRE(data: String)

end ResultEnum

```


### ScratchResult.scala
```scala
 // ScratchResult.scala
package monads
// Scratch/experimental example.
// This doesn't work because the Either is
// contained in Result via composition.
// You can't inherit from Either because it's
// sealed.
// I think the solution is to make my own minimal
// Result only containing map and flatMap.

object ScratchResult: // Keep from polluting the 'monads' namespace
  class Result[F, S](val either: Either[F, S])

  case class Fail[F](fail: F)
      extends Result(Left(fail))

  case class Succeed[S](s: S)
      extends Result(Right(s))

@main
def essence = println("The essence of a monad")

```


### FlowAndLaws.scala
```scala
 // FlowAndLaws.scala
package monads

enum Status:
  case Terminate
  case Continue

import Status.*

// Monads = Inversion of Flow Logic
case class Flow(
    status: Status,
    message: String = ""
):

  def flatMap(f: String => Flow): Flow =
    if (this == Flow.identity(message))
      f(message)
    else
      this

  def map(f: String => Status): Flow =
    flatMap(f.andThen(Flow(_)))

object Flow:

  def identity(message: String): Flow =
    Flow(Continue, message)

def doThing(s: String): Flow =
  println("doThing")
  Flow(Terminate, "terminating")

def doAnotherThing(s: String): Flow =
  println("doAnotherThing")
  Flow(Continue, "trying to unterminate")

@main
def imperative =
  val a = Flow(Continue, "starting")
  if (a.status == Continue)
    val b = doThing(a.message)
    if (b.status == Continue)
      val c = doAnotherThing(b.message)
      Terminate
    else
      b
  else
    a

@main
def monadic =
  println(
    for
      a <- Flow(Continue, "starting")
      b <- doThing(a)
      c <- doAnotherThing(b)
    yield Terminate
  )

// monads are a binary tree control flow
// structure
// the identity function is essential because it
// determines
// the path to take when performing an operation
// on the
// data held by the structure.

// Booleans suck. (they abstract away the
// meaning)

@main
def laws =
  // Left Identity Law
  assert(
    Flow.identity("asdf").flatMap(doThing) ==
      doThing("asdf")
  )

  assert(
    Flow
      .identity("asdf")
      .flatMap(_ =>
        Flow(Terminate, "something")
      ) == Flow(Terminate, "something")
  )

  // Right Identity Law
  // interesting question: if the monad calls the
  // flatMap functor with a value other than
  // what was passed in, does that violate the
  // monad right identity law
  assert(
    Flow
      .identity("original")
      .flatMap(Flow.identity) ==
      Flow.identity("original")
  )

  // starting value can be any monad instance
  assert(
    Flow(Terminate, "original")
      .flatMap(Flow.identity) ==
      Flow(Terminate, "original")
  )

  // Associativity Law
  def reverse(s: String): Flow =
    if (s.isEmpty)
      Flow(Terminate)
    else
      Flow(Continue, s.reverse)
  def upper(s: String): Flow =
    if (s.isEmpty)
      Flow(Terminate)
    else
      Flow(Continue, s.toUpperCase.toString)
  def reverseThenUpper(s: String): Flow =
    reverse(s).flatMap(upper)
  assert(
    Flow
      .identity("asdf")
      .flatMap(reverse)
      .flatMap(upper) ==
      Flow
        .identity("asdf")
        .flatMap(reverseThenUpper)
  )

  assert(
    Flow(Terminate, "asdf")
      .flatMap(reverse)
      .flatMap(upper) ==
      Flow(Terminate, "asdf")
        .flatMap(reverseThenUpper)
  )

  assert(
    Flow
      .identity("")
      .flatMap(reverse)
      .flatMap(upper) ==
      Flow.identity("").flatMap(reverseThenUpper)
  )
end laws

```

