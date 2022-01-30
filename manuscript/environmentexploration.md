## environmentexploration

 

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


### experiments/src/main/scala/environmentexploration/TypeMapEnvironment.scala
```scala
package environmentexploration

import izumi.reflect.macrortti.LightTypeTag
import zio.IsNotIntersection
import izumi.reflect.Tag

object TypeMapEnvironment:
  val t: LightTypeTag = ???

class TypeMapEnvironment[+R](
    private val map: Map[LightTypeTag, Any]
):
  self =>
  def add[A](a: A)(implicit
      ev: IsNotIntersection[A],
      tagged: Tag[A]
  ): TypeMapEnvironment[R with A] =
    new TypeMapEnvironment(
      self.map + (taggedTagType(tagged) -> a)
    )

  def taggedTagType[A](
      tagged: Tag[A]
  ): LightTypeTag = tagged.tag

```

