## effects-temporalVars.md

```scala
package effects

import java.util.Calendar

object temporalVars:

  // Time based functions are effectful because
  // they
  // rely on a variable that is constantly
  // changing.

  def sayTime() =
    val curTime = Calendar.getInstance()
    val curOption: Option[java.util.Calendar] =
      curTime match
        case null =>
          None
        case x: java.util.Calendar =>
          Some(x)
    val curMin =
      curOption match
        case None =>
          println("oof")
        case Some(s) =>
          s.get(Calendar.SECOND)
    println(curMin)

  @main
  def temporalVarsEx =
    sayTime()
    Thread.sleep(3000)
    sayTime()

end temporalVars

// The input for the variable is the same, yet
// there is a different output.
// The clock is thus considered an effect.

```