package models

case class User(name: String, documentNumber: String, enabled: Boolean = true, id: Option[Int] = None)

