package sbt

import zio._
import tui._
import tui.view.{Alignment, KeyEvent, View}
import tui.view.View._

final case class State(
    sbtState: SbtState
)

// ELM / Brick / Redux
object CLI extends TerminalApp[SbtState, State, Unit] {

  // one thing
  // another
  override def render(state: State): View =
    View
      .vertical(
        "ZIO EXERCISES".blue,
        "─────────────".blue.dim,
        renderSbtState(state.sbtState),
        "",
        View.horizontal("q".blue, "quit".blue.dim)
      )
      .padding(1)
      .flex(maxWidth = Some(Int.MaxValue), maxHeight = Some(Int.MaxValue), alignment = Alignment.topLeft)

  private def renderSbtState(sbtState: SbtState): View =
    sbtState match {
      case SbtState.Loading =>
        "Loading...".dim
      case SbtState.Success(message) =>
        message.green
      case SbtState.Error(lines) =>
        View
          .vertical(
            lines.map(View.text(_)): _*
          )
          .red
      case SbtState.TestOutput(lines, _, _) =>
        View
          .vertical(
            lines.map(View.Text(_, None, None)): _*
          )
      case SbtState.Rebuilding(state) =>
        renderSbtState(state).dim
    }

  override def update(
      state: State,
      event: TerminalEvent[SbtState]
  ): TerminalApp.Step[State, Unit] =
    event match {
      case TerminalEvent.UserEvent(sbtState) =>
        TerminalApp.Step.update(state.copy(sbtState = sbtState))

      case TerminalEvent.SystemEvent(KeyEvent.Character('q')) =>
        TerminalApp.Step.exit

      case _ =>
        TerminalApp.Step.update(state)
    }

}
