## game_theory

 

### experiments/src/main/scala/game_theory/DecisionService.scala
```scala
package game_theory

import zio.{Ref, ZIO, ZLayer}

trait DecisionService:
  def getDecisionsFor(
      prisoner1: Prisoner,
      prisoner2: Prisoner
  ): ZIO[Any, String, RoundResult]

object DecisionService:
  class LiveDecisionService(
      history: Ref[DecisionHistory]
  ) extends DecisionService:
    private def getDecisionFor(
        prisoner: Prisoner,
        actionsAgainst: List[Action]
    ): ZIO[Any, String, Decision] =
      for action <-
          prisoner.decide(actionsAgainst)
      yield Decision(prisoner, action)

    def getDecisionsFor(
        prisoner1: Prisoner,
        prisoner2: Prisoner
    ): ZIO[Any, String, RoundResult] =
      for
        prisoner1history <-
          history
            .get
            .map(_.historyFor(prisoner1))
        prisoner2history <-
          history
            .get
            .map(_.historyFor(prisoner2))
        decisions <-
          getDecisionFor(
            prisoner1,
            prisoner2history
          ).zipPar(
            getDecisionFor(
              prisoner2,
              prisoner1history
            )
          )
        roundResult =
          RoundResult(decisions._1, decisions._2)
        _ <-
          history.updateAndGet(oldHistory =>
            DecisionHistory(
              roundResult :: oldHistory.results
            )
          )
      yield roundResult
  end LiveDecisionService

  object LiveDecisionService:
    def make(): ZIO[
      Any,
      Nothing,
      LiveDecisionService
    ] =
      for history <-
          Ref.make(DecisionHistory(List.empty))
      yield LiveDecisionService(history)

  val liveDecisionService: ZLayer[
    Any,
    Nothing,
    LiveDecisionService
  ] = ZLayer.fromZIO(LiveDecisionService.make())
end DecisionService

```


### experiments/src/main/scala/game_theory/SingleBasic.scala
```scala
package game_theory

import game_theory.Action.{Betray, Silent}
import game_theory.Outcome.{
  BothFree,
  BothPrison,
  OnePrison
}
import zio.Console.printLine
import zio.*

case class Decision(
    prisoner: Prisoner,
    action: Action
)
case class RoundResult(
    prisoner1Decision: Decision,
    prisoner2Decision: Decision
):
  override def toString: String =
    s"RoundResult(${prisoner1Decision.prisoner}:${prisoner1Decision
        .action} ${prisoner2Decision.prisoner}:${prisoner2Decision.action})"
case class DecisionHistory(
    results: List[RoundResult]
):
  def historyFor(
      prisoner: Prisoner
  ): List[Action] =
    results.map(roundResult =>
      if (
        roundResult.prisoner1Decision.prisoner ==
          prisoner
      )
        roundResult.prisoner1Decision.action
      else
        roundResult.prisoner2Decision.action
    )

trait Strategy:
  def decide(
      actionsAgainst: List[Action]
  ): ZIO[Any, Nothing, Action]

case class Prisoner(
    name: String,
    strategy: Strategy
):
  def decide(
      actionsAgainst: List[Action]
  ): ZIO[Any, Nothing, Action] =
    strategy.decide(actionsAgainst)

  override def toString: String = s"$name"

val silentAtFirstAndEventuallyBetray =
  new Strategy:
    override def decide(
        actionsAgainst: List[Action]
    ): ZIO[Any, Nothing, Action] =
      if (actionsAgainst.length < 3)
        ZIO.succeed(Silent)
      else
        ZIO.succeed(Betray)

val alwaysTrust =
  new Strategy:
    override def decide(
        actionsAgainst: List[Action]
    ): ZIO[Any, Nothing, Action] =
      ZIO.succeed(Silent)

val silentUntilBetrayed =
  new Strategy:
    override def decide(
        actionsAgainst: List[Action]
    ): ZIO[Any, Nothing, Action] =
      if (actionsAgainst.contains(Betray))
        ZIO.succeed(Betray)
      else
        ZIO.succeed(Silent)

enum Action:
  case Silent
  case Betray

enum Outcome:
  case BothFree,
    BothPrison
  case OnePrison(prisoner: Prisoner)

object SingleBasic extends ZIOAppDefault:

  def play(
      prisoner1: Prisoner,
      prisoner2: Prisoner
  ): ZIO[DecisionService, String, Outcome] =
    for
      decisionService <-
        ZIO.service[DecisionService]
      roundResult <-
        decisionService
          .getDecisionsFor(prisoner1, prisoner2)
          .debug("Decisions")

      outcome =
        (
          roundResult.prisoner1Decision.action,
          roundResult.prisoner2Decision.action
        ) match
          case (Silent, Silent) =>
            BothFree
          case (Betray, Silent) =>
            OnePrison(prisoner2)
          case (Silent, Betray) =>
            OnePrison(prisoner1)
          case (Betray, Betray) =>
            BothPrison
    yield outcome

  val bruce =
    Prisoner(
      "Bruce",
      silentAtFirstAndEventuallyBetray
    )
  val bill =
    Prisoner("Bill", silentUntilBetrayed)

  def run =
    play(bruce, bill)
      .debug("Outcome")
      .repeatN(4)
      .provideLayer(
        DecisionService.liveDecisionService
      )
end SingleBasic

```

