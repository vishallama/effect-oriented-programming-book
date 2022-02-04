## environmentexploration

 

### experiments/src/main/scala/environmentexploration/ToyEnvironment.scala
```scala
package environmentexploration

case class DBService(url: String)

// Yada yada yada lets talk about the environment
trait ToyEnvironmentT[+R]:
  def add[A](a: A): ToyEnvironment[R & A]
  def get[A >: R](classA: Class[A]): A

class ToyEnvironment[+R](
    typeMap: Map[Class[_], Any]
) extends ToyEnvironmentT[R]:
  def add[A](a: A): ToyEnvironment[R & A] =
    ToyEnvironment(typeMap + (a.getClass -> a))

  def get[A >: R](classA: Class[A]): A =
    typeMap(classA).asInstanceOf[A]

@main
def demoTypeMapInsertionAndRetrieval =
  val env: ToyEnvironment[Any] =
    ToyEnvironment[Any](Map.empty)

  val env1: ToyEnvironment[Any & String] =
    env.add("hi")

  val env2: ToyEnvironment[
    Any & String & DBService
  ] = env1.add(DBService("blah"))

  val env3: ToyEnvironment[
    Any & String & DBService & List[String]
  ] = env2.add(List("a", "b"))

  println(env3.get(classOf[String]))
  println(env3.get(classOf[DBService]))
  // Blows up at runtime, because we can't
  // actually cast generic types to concrete
  // types
  println(env3.get(classOf[List[String]]))

// We get some amount of compile time safety here, but not much
// println(env.get(classOf[List[DBService]]))
end demoTypeMapInsertionAndRetrieval

```


### experiments/src/main/scala/environmentexploration/TupledEnvironmentZio.scala
```scala
package environmentexploration

import scalaBasics.forComprehension

// trait TypeTag // TODO Or ClassTag?
// trait TypeInstance

// case class TypeMap(
//   typeMap: Map[TypeTag, TypeInstance]
// )

case class TupledEnvironmentZio[ENV, RESULT](
    run: ENV => RESULT
):
  def unsafeRun(env: ENV): RESULT = run(env)

  // The tuple here is a step towards the
  // full-featured TypeMap that ZIO uses
  def flatMap[ENV2, RESULT2](
      f: RESULT => TupledEnvironmentZio[
        ENV2,
        RESULT2
      ]
  ): TupledEnvironmentZio[(ENV, ENV2), RESULT2] =
    TupledEnvironmentZio((env, env2) =>
      f(run(env)).run(env2)
    )

@main
def demoSingleEnvironmentInstance =
  val customTypeMapZio
      : TupledEnvironmentZio[Int, String] =
    TupledEnvironmentZio(env =>
      val result = env * 10
      s"result: $result"
    )
  println(customTypeMapZio.unsafeRun(5))

  val repeatMessage
      : TupledEnvironmentZio[Int, String] =
    TupledEnvironmentZio(env =>
      s"Message \n" * env
    )
  println(repeatMessage.unsafeRun(5))

case class BigResult(message: String)
@main
def demoTupledEnvironment =
  val squared: TupledEnvironmentZio[Int, Unit] =
    TupledEnvironmentZio(env =>
      println(
        "Environment integer squared: " +
          env * env
      )
    )

  val repeatMessage
      : TupledEnvironmentZio[String, BigResult] =
    TupledEnvironmentZio(message =>
      BigResult(s"Environment message: $message")
    )

  val composedRes: TupledEnvironmentZio[
    (Int, String),
    BigResult
  ] = squared.flatMap(_ => repeatMessage)

  val finalResult =
    composedRes.unsafeRun((5, "Hello"))
  println(finalResult)
end demoTupledEnvironment

import zio.ZIO

```

