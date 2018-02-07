package pme.bot.control

import javax.inject.{Inject, Named, Singleton}

import akka.actor.ActorRef
import pme.bot.entity.SubscrType.SubscrAspect
import pme.bot.entity.{RunAspect, Subscription}
/**
  * Created by pascal.mengelt on 14.01.2017.
  */
@Singleton
class LogStateSubscription @Inject()(@Named("commandDispatcher")
                                     val commandDispatcher: ActorRef) {
  commandDispatcher ! Subscription(RunAspect.logStateCommand, SubscrAspect, None)

}
