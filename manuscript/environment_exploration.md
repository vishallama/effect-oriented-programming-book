## environment_exploration

 

### experiments/src/main/scala/environment_exploration/ToyEnvironment.scala
```scala
package environment_exploration

import scala.reflect.{ClassTag, classTag}

case class DBService(url: String)

// Yada yada yada lets talk about the environment
trait ToyEnvironmentT[+R]:

  def add[A: ClassTag](
      a: A
  ): ToyEnvironmentT[R & A]

  def get[A >: R: ClassTag]: A

class ToyEnvironment[+R](
    typeMap: Map[ClassTag[_], Any]
) extends ToyEnvironmentT[R]:

  def add[A: ClassTag](
      a: A
  ): ToyEnvironment[R & A] =
    ToyEnvironment(typeMap + (classTag[A] -> a))

  def get[A >: R: ClassTag]: A =
    typeMap(classTag[A]).asInstanceOf[A]

@main
def demoToyEnvironment =
  val env: ToyEnvironment[_] =
    ToyEnvironment(Map.empty)

  val env1: ToyEnvironment[String] =
    env.add("hi")

  val env2: ToyEnvironment[String & DBService] =
    env1.add(DBService("blah"))

  val env3: ToyEnvironment[
    String & DBService & List[String]
  ] = env2.add(List("a", "b"))

  println(env3.get[String])
  println(env3.get[DBService])
  println(env3.get[List[String]])

  // We get some amount of compile time safety
  // here, but not much
  // println(env.get(classOf[List[DBService]]))

  // Downside of the current approach is that it
  // doesn't prevent duplicate types
  env3.add("hi") // is accepted
end demoToyEnvironment

// Consider this runtime de-duping

class ToyEnvironmentRuntimeDeduplication[+R](
    typeMap: Map[ClassTag[_], Any]
):

  def add[A: ClassTag](
      a: A
  ): ToyEnvironment[R & A] =
    if (typeMap.contains(classTag[A]))
      throw new IllegalArgumentException(
        s"Cannot add ${classTag[A]} to environment, it already exists"
      )
    else
      ToyEnvironment(
        typeMap + (classTag[A] -> a)
      )

```


### experiments/src/main/scala/environment_exploration/TupledEnvironmentZio.scala
```scala
package environment_exploration

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

