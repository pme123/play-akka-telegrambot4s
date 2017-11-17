package pme.bots.entity

case class CallbackTag(name: String, labelOpt: Option[String] = None) {
  require(name != null && name.length >= 3, "A name is required to have at least 3 characters")
  val label: String = labelOpt.getOrElse(name)
}