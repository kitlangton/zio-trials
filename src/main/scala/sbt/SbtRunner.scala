package sbt

import tui.TUI
import zio._
import zio.process._
import zio.stream._

sealed trait SbtState extends Product with Serializable

object SbtState {
  case object Loading                                                              extends SbtState
  final case class Success(message: String)                                        extends SbtState
  final case class TestOutput(lines: Chunk[String], recap: Boolean, done: Boolean) extends SbtState
  final case class Error(lines: Chunk[String])                                     extends SbtState
  final case class Rebuilding(state: SbtState)                                     extends SbtState
}

object SbtRunner {

  def stripAnsiCodes(string: String): String =
    string.replaceAll("\u001B\\[[;\\d]*m", "")

  object RemovingAnsi {
    def unapply(string: String): Option[String] =
      Some(stripAnsiCodes(string))
  }

  val compileStream: Stream[CommandError, String] =
    Command("sbt", "~test", "--color=always").linesStream

  val sbtStateStream: Stream[CommandError, SbtState] =
    compileStream
//      .tap(ZIO.debug(_))
      .scan[SbtState](SbtState.Loading) {
        case (_, s"[success] $message") =>
          SbtState.Success(message)

        case (state, line)
            if !state.isInstanceOf[SbtState.TestOutput] &&
              line.contains("☃ Ex.") =>
          SbtState.TestOutput(Chunk(line), recap = false, done = false)

        case (SbtState.TestOutput(lines, recap, false), line @ RemovingAnsi(s"[error] Total time: $_")) =>
          SbtState.TestOutput(lines.appended(line), recap, done = true)

        case (SbtState.TestOutput(lines, _, done), s"$_ tests passed. $_") =>
          SbtState.TestOutput(lines, recap = true, done)

        case (SbtState.TestOutput(lines, true, false), _) =>
          SbtState.TestOutput(lines, recap = true, done = false)

        case (SbtState.TestOutput(lines, false, false), line) =>
          SbtState.TestOutput(lines.appended(line), recap = false, done = false)

        case (SbtState.Error(lines), RemovingAnsi(s"[error] $message")) =>
          SbtState.Error(lines.appended(message))

        case (_, RemovingAnsi(s"[error] $message")) =>
          SbtState.Error(Chunk(message))

        case (rebuilding @ SbtState.Rebuilding(_), s"[info] Build triggered $_") =>
          rebuilding

        case (state, RemovingAnsi(s"[info] Build triggered $_")) =>
          SbtState.Rebuilding(state)

        case (state, _) =>
          state
      }

}

//object SbtRunnerDemos extends ZIOAppDefault {
//  val program =
//    SbtRunner.sbtStateStream
//      .foreach(ZIO.debug(_))
//
//  val run = program
//}

object SbtRunnerTest extends ZIOAppDefault {
  val program =
    TUI.runWithEvents(CLI)(
      SbtRunner.sbtStateStream,
      State(SbtState.Loading)
    )

  val run = program
    .provide(
      TUI.live(true)
    )
}
