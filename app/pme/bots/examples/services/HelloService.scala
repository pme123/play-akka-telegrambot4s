package pme.bots.examples.services

import javax.inject.{Inject, Named, Singleton}

import akka.actor.{ActorRef, ActorSystem, Props}
import pme.bots.control.ChatService
import pme.bots.entity.SubscrType.SubscrService
import pme.bots.entity.{Command, Subscription}

/**
  * The user sends command /hello
  * The Bot answers with hello to the user
  * Example of a ChatService.
  * That is if there is no state at all (no conversation)
  */
case class HelloService()
  extends ChatService { // this is a Service

  // the standard actor receive method
  def receive: Receive = {
    // a service only receives the message without any callback data
    case Command(msg, _) =>
      // return a simple hello with a Bot helper
      bot.sendMessage(msg, s"hello ${msg.from.map(_.firstName)}")
    case other =>
      warn(s"Not expected message: $other")
  }
}

object HelloService {
  // the command to listen for
  val command = "/hello"

  // constructor of the Service - which is an Actor
  def props: Props = Props(HelloService())
}

// a singleton will inject all needed dependencies and subscribe the service
@Singleton
class HelloServiceSubscription @Inject()(@Named("commandDispatcher")
                                              val commandDispatcher: ActorRef
                                              , val system: ActorSystem) {
import HelloService._

  // subscribe the HelloService to the CommandDispatcher
  commandDispatcher ! Subscription(command, SubscrService
    , Some(_ => system.actorOf(props)))

}
