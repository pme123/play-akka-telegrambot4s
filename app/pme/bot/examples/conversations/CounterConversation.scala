package pme.bot.examples.conversations

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem, Props}
import pme.bot.control.ChatConversation
import pme.bot.entity.SubscrType.SubscrConversation
import pme.bot.entity.{Command, FSMData, FSMState, Subscription}

// @formatter:off
/**
  * counts the number user pushes the button and the number of requests
  *
  *     [Idle]
  *       v
  *   [Counting] <--------
  *       v              |
  *   markupCounter ------
  */
// @formatter:on
class CounterConversation
  extends ChatConversation { // it's a conversation

  private var requestCount = 0

  when(Idle) {
    case Event(Command(msg, _), _) =>
      bot.sendMessage(msg, "Press to increment!"
        , countButton(0))
      // tell where to go next with the first count
      goto(Counting) using Count(0)
    case other => notExpectedData(other)
  }

  when(Counting) { // when the state is Counting, this function is called
    // FSM returns an Event that contains:
    //  - the Command from the CommandDispatcher
    //  - the State from the last step (using Count(n))
    case Event(Command(msg, _), Count(n)) =>
      val count = n + 1
      requestCount += 1
      // send the updated button using the BotFacade
      bot.sendEditMessage(msg, countButton(count))
      // this is a simple conversation that stays always in the same state.
      // pass the State to the next step
      stay() using Count(count)
  }

  private def countButton(count: Int) = {
    bot.createDefaultButtons(s"Press me!!!\n$count - $requestCount")
  }

  // state to indicate that the count button is already shown to the User
  case object Counting extends FSMState

  // the data of the actual count
  case class Count(count: Int) extends FSMData
}

object CounterConversation {
  val command = "/counter"

  def props: Props = Props(new CounterConversation())
}

@Singleton
class CounterServiceSubscription @Inject()(@Named("commandDispatcher")
                                              val commandDispatcher: ActorRef
                                              , val system: ActorSystem) {
import CounterConversation._

  commandDispatcher ! Subscription(command, SubscrConversation
    , Some(_ => system.actorOf(props)))

}
