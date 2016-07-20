package models

import helpers.SlickDatabase._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.backend.DatabasePublisher
import slick.driver.H2Driver.api._
import slick.lifted.TableQuery

import scala.concurrent.Future

case class User(name: String, documentNumber: String, enabled: Boolean, id: Option[Int])

class Users(tag: Tag) extends Table[User](tag, "users") {
  def name           = column[String]("name")
  def documentNumber = column[String]("documentNumber")
  def enabled        = column[Boolean]("enabled")
  def id             = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
  def *              = (name , documentNumber, enabled, id) <> ((User.apply _).tupled, User.unapply)
}

object User {
  private val userTableQuery = TableQuery[Users]

  type UserQuery = Query[Users, User, Seq]
  implicit class FilterHelper[UserQuery](q: UserQuery) {
    def ifThen[B](cond: Boolean) (f: UserQuery => UserQuery ): UserQuery = {
      if (cond) f(q) else q
    }
  }

  def userSeq(nameOpt: Option[String] = None): Future[Seq[User]] = {
    val query = userTableQuery
      .sortBy( _.name.asc )
      .ifThen(nameOpt.isDefined) { _.filter( _.name   ===   nameOpt.get ) }
      .result

    db.run(query)
  }

  def userStream(nameOpt: Option[String] = None): DatabasePublisher[User] = {
    val query = userTableQuery
      .sortBy( _.name.asc )
      .ifThen(nameOpt.isDefined) { _.filter( _.name   ===   nameOpt.get ) }
      .result

    db.stream(query)
  }

  def count(): Future[Int] = {
    db.run{ userTableQuery.length.result }
  }

  def createDb() = {
    db.run(userTableQuery.schema.create)
  }

  def destroyDb() = {
    db.run(userTableQuery.schema.drop)
  }

  def populateDbWithMax(maxRows: Int = 99999): Future[Unit] = {
    db.run(userTableQuery.length.result).flatMap { length =>
      val numRows= maxRows - length
      db.run(
        DBIO.seq(
          userTableQuery ++= (1 to numRows).map( i => User("Logan_" + i, i + "00", true, None) ).toSeq
        )
      )
    }
  }

  def create(numRows: Int): Future[Unit] = {
    db.run(
      DBIO.seq(
        userTableQuery ++= (1 to numRows).map( i => User("Logan_" + i, i + "00", true, None) ).toSeq
      )
    )
  }

  def deleteAll(): Future[Int] = {
    db.run(userTableQuery.delete)
  }

}
