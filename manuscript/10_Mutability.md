

## Automatically attached experiments.
 These are included at the end of this
 chapter because their package in the
 experiments directory matched the name
 of this chapter. Enjoy working on the
 code with full editor capabilities :D

 

### experiments/src/main/scala/mutability/Refs.scala
```scala
package mutability

import zio.{Ref, ZIO, ZIOAppDefault}

object Refs extends ZIOAppDefault:
  def run =
    for
      ref        <- Ref.make(1)
      startValue <- ref.get
      _ <-
        ZIO.debug("start value: " + startValue)
      _          <- ref.set(5)
      finalValue <- ref.get
      _ <-
        ZIO.debug("final value: " + finalValue)
    yield ()

```

            