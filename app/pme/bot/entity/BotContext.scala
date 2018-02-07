package pme.bot.entity

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._
import scala.language.implicitConversions
/**
  * created by pascal.mengelt
  * This config uses the small framework typesafe-config.
  * See here the explanation: https://github.com/typesafehub/config
  */
object BotSettings extends Logger {
  val configPath = "pme.bot"

  val tokenProp = "token"
  val acceptUsersProp = "accept.users"

  val jobConfigsProp = "job.configs"

  def config(): Config = {
    ConfigFactory.invalidateCaches()
    ConfigFactory.load()
  }
}

// this settings will be validated on startup
class BotSettings(config: Config) extends Logger {
  import BotSettings._

  // checkValid(), just as in the plain SimpleLibContext
  config.checkValid(ConfigFactory.defaultReference(), configPath)

  def createUrl(host: String, port: Int, sslEnabled: Boolean): String = {
    val protocol = if (sslEnabled) "https" else "http"
    s"$protocol://$host:$port"
  }

  val projectConfig: Config = config.getConfig(configPath)

  // note that these fields are NOT lazy, because if we're going to
  // get any exceptions, we want to get them on startup.
  val token: String = projectConfig.getString(tokenProp)
  val acceptUsers: Seq[Int] = projectConfig.getIntList(acceptUsersProp).asScala.map(_.intValue())

}

import pme.bot.entity.BotSettings._

// This is a different way to do BotContext, using the
// BotSettings class to encapsulate and validate the
// settings on startup
class BotContext(config: Config) {
  lazy val settings = new BotSettings(config)

  def acceptsUser(userId: Int): Boolean =
    settings.acceptUsers.isEmpty || settings.acceptUsers.contains(userId)

}

// default Configuration
object BotContext extends BotContext(config()) {
}
