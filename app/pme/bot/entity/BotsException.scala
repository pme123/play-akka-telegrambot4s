package pme.bot.entity

/**
  * Marker trait for all internal Exceptions, that are handled.
  */
trait BotsException
  extends RuntimeException {
  def msg: String

  def cause: Option[Throwable] = None

  override def getMessage: String = msg

  override def getCause: Throwable = {
    cause.orNull
  }

  lazy val allErrorMsgs: String = {
    def inner(throwable: Throwable, last: Throwable): String =
      if (throwable == null || throwable == last) ""
      else {
        val causeMsg = inner(throwable.getCause, throwable)
        throwable.getMessage + (if (causeMsg.nonEmpty) s"\n - Cause: $causeMsg" else "")
      }

    inner(this, null)
  }
}

case class MissingArgumentException(msg: String)
  extends BotsException

case class BadArgumentException(msg: String)
  extends BotsException

case object RootManipulationException
  extends BotsException {
  val msg = "It is not allowed to delete the ROOT Directory!"
}

case class JsonParseException(msg: String, override val cause: Option[Throwable] = None)
  extends BotsException {
}

case class UploadDataException(msg: String)
  extends BotsException
