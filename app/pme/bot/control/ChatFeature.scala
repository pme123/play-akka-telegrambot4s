package pme.bot.control

import akka.actor.{Actor, FSM}
import pme.bot.entity.LogLevel._
import pme.bot.entity._


trait ChatService
  extends Actor
    with Logger {
  val bot = BotFacade()
}

trait ChatConversation
  extends FSM[FSMState, FSMData]
    with Logger {

  val bot = BotFacade()

  import pme.bot.entity.RunAspect._
  // starts every conversation
  startWith(Idle, NoData)

  // handle async execution,
  // see http://stackoverflow.com/questions/29489564/akka-fsm-goto-within-future
  when(WaitingForExecution) {
    case Event(ExecutionResult(state, data), _) =>
      goto(state) using data
    case Event(Stay, _) =>
      stay()
  }

  whenUnhandled {
    case Event(RestartCommand, _) => goto(Idle)
    case Event(RunAspect(`logStateCommand`, msg), data) =>
      bot.sendMessage(msg, "Logged state:\n" + data)
      stay()
    case Event(RunAspect(other, msg), _) =>
      bot.sendMessage(msg, s"Sorry this Aspect '$other' is not supported by this conversation.")
      stay()
    case event@Event(Command(msg, _), other) =>
      bot.sendMessage(msg, s"Sorry I could not handle your message. You need to start over with a command. - $other")
      notExpectedData(event)
    case event@Event(_, _) =>
      notExpectedData(event)
  }

  def newConversation(): State = goto(Idle)

  protected val showReport = "Show Report"


  protected def notExpectedData(other: Event): State = {
    log.warning(s"received unhandled request ${other.event} in state $stateName/${other.stateData}")
    stay()
  }

  def reportText(logReport: LogReport): String = logReport.maxLevel() match {
    case ERROR => "There were some Exceptions!\n"
    case WARN => "There were some Warning!\n"
    case _ => "Everything went just fine!\n"
  }

  // start state of any conversation
  case object Idle extends FSMState

  // to handle async execution you can use this state (goto(WaitingForExecution))
  case object WaitingForExecution extends FSMState

  // if there is no data use this (e.g. when Idle)
  case object NoData extends FSMData

  // used for sending the next state and data after an async execution
  case class ExecutionResult(fSMState: FSMState, fSMData: FSMData)

  // used to indicate that there is no state change after an async exection
  case object Stay

}
