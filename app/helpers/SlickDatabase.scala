package helpers

import slick.driver.H2Driver.api._

object SlickDatabase {
  lazy val db = Database.forConfig("slick.h2")
}