# play-akka-telegrambot4s
Small library to handle multiple conversations with a telegram bot.

# This project is under development!

This library should help you with:

* Handling the state of each user.
* Create a FSM (finite state machine) to design the conversation.
* Provide you some helpers needed in your conversations.
* Mix in stateless Services
* Allow you to add aspects to your conversations.

Like the name suggests it's supporting only Telegram. The idea is to extend that to other 
messaging provider at a later time.

# Token configuration
It allows you to add the token in the classpath (add `bot.token`-file in the `conf`-directory).

Or pass it as a system property, like `-DBOT_TOKEN=[token]`

# Chat Features
We support 2 kinds of _chat-features_:

## 1. Service (stateless)
To get an information from the Bot that does not need any interaction with 
the Bot, you can use a Service (stateless request). 
Here an example:
```scala
/**
  * The user sends command /hello
  * The Bot answers with hello to the user
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
```
Provide a standard constructor for an Actor:
```scala
object HelloService {
  // the command to listen for
  val command = "/hello"

  // constructor of the Service - which is an Actor
  def props: Props = Props(HelloService())
}
```
And finally subscribe the Service:
```scala
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
```
Here is the complete example: 
[HelloService](https://raw.githubusercontent.com/pme123/play-akka-telegrambot4s/master/app/pme/bots/examples/services/HelloService.scala)

## 2. Conversation (statelful)
That's what you actually understand as a chat. 
A stateful conversation where the Bot guides the user through a defined process (workflow).

For a conversation we use a Finite State Machine from Akka: `akka.actor.FSM` which is also an Actor.

Here a simple conversation: 
[CounterConversation](https://raw.githubusercontent.com/pme123/play-akka-telegrambot4s/master/app/pme/bots/examples/conversations/CounterConversation.scala)

Here the main difference is that we now have a state that must be handled:
```scala
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
```
Check out the [Akka Documentation](https://doc.akka.io/docs/akka/2.5/scala/fsm.html)

A more sophisticated example you find here: 
[Telegram Bot with Play Framework, Akka FSM, Scala.js, Binding.scala](https://github.com/pme123/play-akka-telegrambot4s-incidents)


# Run Aspect
This allows you to add functionality to all conversation. For example
get the state of the current conversation.

# Usage
Add resolver: 

`resolvers += "jitpack" at "https://jitpack.io"`

Add dependency:

`libraryDependencies += "com.github.pme123" % "play-akka-telegrambot4s" % "0.0.5"`

Check for the latest version on 
[Jitpack.io](https://jitpack.io/#pme123/play-akka-telegrambot4s) 