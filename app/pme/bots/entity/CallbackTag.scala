package pme.bots.entity

trait CallbackTag {

  def name: String

  def labelOpt: Option[String] = None

  def label: String = labelOpt.getOrElse(name)

  require(name != null && name.length >= 3, "A name is required to have at least 3 characters")

}