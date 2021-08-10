# Monads

> A function can take any number of inputs, but it can only return a single result.

We often need to convey more information than can fit into a simple result.
The programmer is forced to use side effects to express all the outcomes of a function call.
Side effects produce unpredictable results and an unpredictable program is unreliable.

The problem is that a single simple result is *too* simple.
What we need is a complex result capable of holding all necessary information that comes out of a function call.

To solve the problem we put all that information into a box: the original result together with any extra information (such as error conditions).
We return that box from the function.

Now we've got boxes everywhere, and programming becomes quite messy and complicated.
Every time you call a function, you must unpack and analyze the contents of the box that comes out as the result.
If there's a problem, you must handle it right after the function is called, which is awkward and often produces duplicate code.
People probably won't use our system unless we figure out a way to simplify and automate the use of these boxes.

What if we had a standard set of operations that work on all boxes, to make our system easy to use by eliminating all that duplicated code?
The box---and these associated operations---is a monad.

## The Error Monad

Initially, the most compelling reason to use monads is error handling.

Encountering an error during a function call generally means two things:

1. You can't continue executing the function in the normal fashion.

2. You can't return a normal result.

Many languages use *exceptions* for handling errors.
An exception *throws* out of the current execution path to locate a user-written *handler* to deal with the error.
There are two goals for exceptions:

1. Separate error-handling code from "success-path" code, so the success-path code is easier to understand and reason about.

2. Reduce redundant error-handling code by handling associated errors in a single place.

What if we make a box called `Result` containing *both* the success-path value together with error information if it fails?
For simplicity, both the error information and the success data are `String`s:

```scala
// Monads/result.scala
case class Fail(why: String)     extends Result
case class Success(data: String) extends Result
```

If you reach a point in a function where something goes wrong, you return a `Fail` with failure information stored in `why`.
If you get all the way through the function without any failures, you return a `Success` with the return calculation stored in `data`.

The Scala `for` comprehension is designed to work with monads.
The `<-` in a `for` comprehension *automatically checks and unpacks a monad!*
The monad does not have to be a standard or built-in type; you can write one yourself as we've done with `Result`.
Let's see how `Result` works:

```scala
// Monads/ShowResult.scala

def show(n: Char) =
  def op(id: Char, msg: String): Result =
    val result =
      if n == id then
        Fail(msg + id.toString)
      else
        Success(msg + id.toString)
    println(s"$n => op($id): $result")
    result
  end op

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
```

`show()` takes `n: Char` indicating how far we want to get through the execution of `compose` before it fails.
Note that `n` is in scope within the nested function `op()`, which compares `n` to its `id` argument.
If they're equal, it returns a `Fail` object, otherwise it returns a `Success` object.

The `for` comprehension within `compose` attempts to execute three calls to `op()`, each of which takes the next value of `id` in alphabetic succession.
Each expression uses the backwards-arrow `<-` to assign the result to a `String` value.
That value is passed to `op()` in the subsequent expression in the comprehension.
If all three expressions execute successfully, the `yield` expression uses `c` to produce the final `Result` value which is assigned to `compose`.

What happens if a call to `op()` fails?
We'll call `show()` with successive values of `n` from `'a'` to `'d'`:

```scala
show('a')
// a => op(a): Fail(a)
// flatMap() on Fail(a)
// compose: Fail(a)
// Error-handling for a
```

`op('a', "")` immediately fails when `n = 'a'`, so the result returned from `op()` is `Fail(a)`.

Here's where things get especially interesting.
When Scala sees `<-` in a `for` comprehension, it automatically calls `flatMap()`.
So `flatMap()` is called on the result of of `op('a', "")`.
That result is `Fail` and *no further lines in `compose` are executed*.
The `a` to the left of the `<-` is never initialized, nor are `b` or `c`.
The resulting value of `compose` becomes the value returned by `flatMap()`, which is `Fail(a)`.

The last lines in `show()` check for failure and execute error-handling code if `Fail` is found.
All the error-handling for `compose` is in one place, in the same way that a `catch` clause combines error-handling code.

```scala
show('b')
// b => op(a): Success(a)
// flatMap() on Success(a)
// b => op(b): Fail(ab)
// flatMap() on Fail(ab)
// compose: Fail(ab)
// Error-handling for ab
```

With `n = 'b'`, the first expression in the `for` comprehension is now successful.
The value of `a` is successfully assigned, then passed into `op('b', a)` in the second expression.
Now the second expression fails and the resulting value of `compose` becomes `Fail(ab)`.
Once again we end up in the error-handling code.

```scala
show('c')
// c => op(a): Success(a)
// flatMap() on Success(a)
// c => op(b): Success(ab)
// flatMap() on Success(ab)
// c => op(c): Fail(abc)
// map() on Fail(abc)
// compose: Fail(abc)
// Error-handling for abc
```

Now we get all the way to the third expression in the `for` comprehension before it fails.
But notice that in this case `map()` is called rather than `flatMap()`.
The last `<-` in a `for` comprehension calls `map()` instead of `flatMap()`, for reasons that will become clear.

Finally, `n = 'd'` successfully makes it through the entire initialization for `compose`:

```scala
show('d')
// d => op(a): Success(a)
// flatMap() on Success(a)
// d => op(b): Success(ab)
// flatMap() on Success(ab)
// d => op(c): Success(abc)
// map() on Success(abc)
// Yielding: abc + 'd'
// compose: Success(abcd)
// Successful case: abcd
```

The return value of `op('c', b)` is `Success(abc)` and this is used to initialize `c`.

The `yield` expression produces the final result that is assigned to `compose`.
You should find all potential problems by the time you reach `yield`, so the `yield` expression should not be able to fail.
Note that `c` is of type `String` but `compose` is of type `Result`.
The `yield` expression is automatically wrapped in a `Success` object.

The identifier name for `val compose` is intentional.
We are composing a result from multiple expressions and the whole `for` comprehension will either succeed or fail, and have its own error handling.

From the above output, the compiler responds to a `<-` within a `for` comprehension by calling `flatMap()` or `map()`.
Thus, it looks like our `Result` must have `flatMap()` and `map()` methods in order to allow these calls.
Here's the definition of `Result`:

```scala
// Monads/Result.scala

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

end Result
```

The code in the two methods is almost identical.
Each receives a function `f` as an argument.
Each checks the subtype of the current (`Result`) object.
A `Fail` just returns that `Fail` object, and never calls `f`.
Only a `Success` causes `f` to be evaluated.
In `flatMap()`, `f` is called on the contents of the `Success`.
In `map()`, `f` is also called on the contents of the `Success`, and then the result of that call is wrapped in another `Success` object.

## Predefined Monads

Because the `for` comprehension provides direct support for monads, you might not be surprised to discover that Scala comes with some predefined monads.
The two most common of these are `Either` and `Option`.
These are generic so they work with any type.

`Either` looks just like our `Result` monad but with different names.
People commonly use `Either` to produce the same effect as `Result`.
Our `Fail` becomes `Left` in `Either`, and our `Success` becomes `Right`.
`Either` has numerous additional methods beyond `map()` and `flatMap()`, so it is much more full-featured.

X> **Exercise 1:** Modify `ShowResult.scala` to use `Either` instead of `Result`.
X> Your output should look like this:


```scala
'a' to 'd' foreach eshow
// >> show(a) <<
// op(a): Left(a)
// Left(a)
// Error-handling for a
// >> show(b) <<
// op(a): Right(a)
// op(b): Left(ab)
// Left(ab)
// Error-handling for ab
// >> show(c) <<
// op(a): Right(a)
// op(b): Right(ab)
// op(c): Left(abc)
// Left(abc)
// Error-handling for abc
// >> show(d) <<
// op(a): Right(a)
// op(b): Right(ab)
// op(c): Right(abc)
// Completed: abc
// Right(abc)
```

X> **Exercise 2:** Modify the solution to Exercise 1 to work with `Int` instead of `String`.
X> Change `msg` in the `op()` argument list to `i`, an `Int`.
X> Your output should look like this:


```scala
'a' to 'd' foreach ishow
// >> show(a) <<
// op(a): Left(97)
// Left(97)
// Error-handling for 97
// >> show(b) <<
// op(a): Right(97)
// op(b): Left(195)
// Left(195)
// Error-handling for 195
// >> show(c) <<
// op(a): Right(97)
// op(b): Right(195)
// op(c): Left(294)
// Left(294)
// Error-handling for 294
// >> show(d) <<
// op(a): Right(97)
// op(b): Right(195)
// op(c): Right(294)
// Completed: 294
// Right(294)
```

`Option` is like `Either` except that the `Right`-side (success) case becomes `Some` (that is, it has a value) and the `Left`-side (failure) case becomes `None`.
`None` simply means that there is no value, which isn't necessarily an error.
For example, if you look something up in a `Map`, there might not be a value for your key, so returning an `Option` of `None` is a common and reasonable result.

X> **Exercise 3:** Modify `ShowResult.scala` to work with `Option` instead of `Result`.
X> Your output should look like this:


```scala
'a' to 'd' foreach oshow
// >> show(a) <<
// op(a): None
// None
// Error-handling for None
// >> show(b) <<
// op(a): Some(a)
// op(b): None
// None
// Error-handling for None
// >> show(c) <<
// op(a): Some(a)
// op(b): Some(ab)
// op(c): None
// None
// Error-handling for None
// >> show(d) <<
// op(a): Some(a)
// op(b): Some(ab)
// op(c): Some(abc)
// Completed: abc
// Some(abc)
```

X> **Exercise 4:** Modify `Result.scala` so `Result` is an `enum` instead of a `trait`.
X> Demonstrate that the `enum Result` works by modifying `ShowResult.scala`.
X> Your output should look like this:



```scala
'a' to 'd' foreach showRE
// op(a): FailRE(a)
// flatMap() on FailRE(a)
// FailRE(a)
// Error-handling for a
// op(a): SuccessRE(a)
// flatMap() on SuccessRE(a)
// op(b): FailRE(ab)
// flatMap() on FailRE(ab)
// FailRE(ab)
// Error-handling for ab
// op(a): SuccessRE(a)
// flatMap() on SuccessRE(a)
// op(b): SuccessRE(ab)
// flatMap() on SuccessRE(ab)
// op(c): FailRE(abc)
// map() on FailRE(abc)
// FailRE(abc)
// Error-handling for abc
// op(a): SuccessRE(a)
// flatMap() on SuccessRE(a)
// op(b): SuccessRE(ab)
// flatMap() on SuccessRE(ab)
// op(c): SuccessRE(abc)
// map() on SuccessRE(abc)
// Completed: abc
// SuccessRE(ABC)
// Success: ABC
```

## Understanding the `for` Comprehension

At this point you should have a sense of what a `for` comprehension is doing, but *how* it does it is still a bit mysterious.
Using the `Either` predefined monad, we can produce a clearer understanding.
Here, each expression in `compose1` uses `Right`, so each one can never fail.
This doesn't matter because we just want to look at the structure of the code:

```scala
val fc1 =
  for
    a <- Right("A")
    b <- Right("B")
    c <- Right("C")
  yield s"Result: $a $b $c"
// fc1: Either[Nothing, String] = Right("Result: A B C")
```

Because we never created a `Left`, Scala decided that the `Left` type should be `Nothing`.

IntelliJ IDEA provides a nice tool that expands this comprehension to show the calls to `flatMap()` and `map()`.
If you select the `for`, you'll see a little light bulb appear.
Click on that and select "Desugar for comprehension."
The result looks like this:

```scala
val fc2 =
  Right("A").flatMap(a =>
    Right("B").flatMap(b =>
      Right("C").map(c => s"Result: $a $b $c")
    )
  )
// fc2: Either[Nothing, String] = Right("Result: A B C")
```

The `for` comprehension left-arrow `<-` generates a call to `flatMap()`.
Notice that the argument to `flatMap()` is a function.
Look back at `flatMap()` in `Result.scala`.
In the `Fail` case (which is `Left` for `Either`), `flatMap` just returns the `Fail` object and *doesn't call that function.*
Here, in the `Right` case (which is like `Success` for `Result`), the function is called and produces `Right("B")` ... with another call to `flatMap()`.
Now the argument is another function, which is again not called in the `Left` case.
In the `Right` case, it returns `Right("C")` ... with another call, but this time to `map()`.
The argument to `map()` is another function, again not called in the `Left` case.
In the `Right` case, it returns something different: the `yield` expression.
Also, `map()` wraps the `yield` expression in a `Right`, unlike `flatMap()`.

Because of this cascade of functions inside function calls, any `flatMap()` or `map()` called on a `Left` result *will not evaluate the rest of the cascade.*
It stops the evaluation and returns `Left` at that point, and the `Left` becomes the result of the expression.
This cascaded expression is thus only evaluated up to the point where a `Left` first appears.
The rest of the expression can be thought of as being short-circuited at that point.

There's another benefit of this cascade of function calls: `a`, `b` and `c` are all in scope by the time you reach the `yield` expression that is the `map()` argument.

X> **Exercise 5:** Modify `fc1` to use `Some` instead of `Either`.
X> Verify it works, then produce the "desugared" version as you see with `fc2`.
X> Your output should look like this:


```scala
sol5a
// res8: Option[String] = Some("Result: A B C")
sol5b
// res9: Option[String] = Some("Result: A B C")
```

## Summary

Think back to the first time you grasped the way that dynamic binding worked to produce virtual function behavior.
In particular, the realization that this pattern is so important that it has been directly implemented by the compiler.
There was probably some sense that the pattern of inheritance polymorphism is fundamental to object-oriented programming.

In this chapter you've experienced a similar realization, but for functional programming.
Producing result information in a monad is so fundamental to functional programming that the Scala compiler provides direct support for this pattern, in the form of the `for` comprehension.