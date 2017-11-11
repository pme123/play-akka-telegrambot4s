package pme.bots.examples.conversations

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem, Props}
import info.mukel.telegrambot4s.api.Extractors
import info.mukel.telegrambot4s.models.{InlineKeyboardButton, InlineKeyboardMarkup}
import pme.bots.callback
import pme.bots.control.ChatConversation
import pme.bots.entity.SubscrType.SubscrConversation
import pme.bots.entity.{Command, FSMState, Subscription}

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
        , replyMarkup = Some(markupCounter(0)))
      // tell where to go next
      goto(Counting)
    case other => notExpectedData(other)
  }

  when(Counting) { // when the state is Counting, this function is called
    case Event(Command(msg, callbackData: Option[String]), _) =>
      for {
        data <- callbackData // extract the callbackData
        Extractors.Int(n) = data
      } {
        // send the updated button
        bot.sendEditMessage(msg, markupCounter(n + 1))
      }
      // this is a simple conversation that stays always in the same state.
      stay()
  }

  private  def markupCounter(n: Int): InlineKeyboardMarkup = {
    requestCount += 1
    InlineKeyboardMarkup.singleButton(
      InlineKeyboardButton.callbackData(
        s"Press me!!!\n$n - $requestCount",
        callback + n))
  }

  // state to indicate that the count button is already shown to the User
  case object Counting extends FSMState
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
