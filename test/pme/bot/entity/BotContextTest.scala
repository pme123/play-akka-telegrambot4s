package pme.bot.entity

import BotContext.settings
import org.scalatest.FlatSpec

class BotContextTest
  extends FlatSpec {

  val token = "xxx127883:AAHvCmoIHnvIBKWi9AEZyxxo31TNxTbFPQY"
  val acceptUsers = Seq(275469757, 299144871)

  "The BotContext" should "retrieve the botToken" in {
    assert(settings.token === token)
  }
  it should "retrieve the acceptUsers" in {
    assert(settings.acceptUsers === acceptUsers)
  }
  it should "accept a User who is in the acceptUsers list" in {
    assert(BotContext.acceptsUser(275469757))
  }
  it should "NOT accept a User who is  NOT in the acceptUsers list" in {
    assert(!BotContext.acceptsUser(575469757))
  }
  it should "accept a User who is  NOT in the acceptUsers list IF no acceptUsers list is defined" ignore {
    // set prop did not work
    System.setProperty(s"${BotSettings.configPath}.${BotSettings.acceptUsersProp}", "[]")
    new BotContext(BotSettings.config())
    assert(BotContext.acceptsUser(575469757))
  }
}
