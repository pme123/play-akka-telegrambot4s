package pme.bots.control

import info.mukel.telegrambot4s.api.declarative.{Callbacks, Commands}
import info.mukel.telegrambot4s.api.{Polling, TelegramBot}
import info.mukel.telegrambot4s.methods._
import info.mukel.telegrambot4s.models.{InlineKeyboardMarkup, _}
import pme.bots.{botToken, callback}

import scala.concurrent.Future

case class BotFacade() extends TelegramBot
  with Polling
  with Commands
  with Callbacks {

  def token: String = botToken

  def sendMessage(msg: Message, text: String): Future[Message] =
    request(SendMessage(msg.source, text, parseMode = Some(ParseMode.HTML)))

  def sendMessage(msg: Message, text: String, replyMarkup: Option[ReplyMarkup] = None): Future[Message] =
    request(SendMessage(msg.source, text, parseMode = Some(ParseMode.HTML), replyMarkup = replyMarkup))

  def sendDocument(msg: Message, inputFile: InputFile): Future[Message] =
    request(SendDocument(msg.source, inputFile))

  def sendEditMessage(msg: Message, markup: Option[InlineKeyboardMarkup]): Future[Either[Boolean, Message]] =
    request(
      EditMessageReplyMarkup(
        Some(ChatId(msg.source)), // msg.chat.id
        Some(msg.messageId),
        replyMarkup = markup))

  def createDefaultButtons(labels: String*): Some[InlineKeyboardMarkup] =
    Some(InlineKeyboardMarkup(
      labels.map(label => Seq(
        InlineKeyboardButton(label, callbackData = Some(callback + label))))))

  def fileName(fileId: String, path: String): String =
    fileId.substring(10) + path.substring(path.lastIndexOf("/") + 1)

  def getFilePath(msg: Message, maxSize: Option[Int] = None): Future[Option[(String, String)]] = {
    val optFileId: Option[String] =
      msg.document.map(_.fileId)
        .orElse(msg.video.map(_.fileId))
        .orElse(extractPhoto(msg, maxSize))

    optFileId match {
      case Some(fileId: String) =>
        request(GetFile(fileId)).map { (file: File) =>
          file.filePath match {
            case Some(path) =>
              Some((file.fileId, fileUrl(path)))
            case _ =>
              sendMessage(msg, s"I could not retrieve the File from the fileId: $fileId")
              None
          }
        }
      case other =>
        sendMessage(msg, "Sorry but you have to add a file to the chat. (Use button <i>send file</i>)\n" +
          s"Not expected: $other / $msg")
        Future(None)
    }
  }

  private def extractPhoto(msg: Message, maxSize: Option[Int]): Option[String] = {
    maxSize match {
      case None => msg.photo.map(_.last.fileId)
      case Some(size) => msg.photo.map(ps =>
        ps.tail.foldLeft[String](ps.head.fileId)((acc, ps: PhotoSize) =>
          if (ps.fileSize.isDefined && ps.fileSize.get <= size) ps.fileId else acc))
    }

  }

  private def fileUrl(filePath: String) =
    s"https://api.telegram.org/file/bot$token/$filePath"

}
