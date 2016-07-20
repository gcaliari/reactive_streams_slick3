package models

import javax.inject.Inject

import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.backend.DatabasePublisher
import slick.driver.JdbcProfile

import scala.concurrent.Future

class UserRepository  @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._

  private val userTable = TableQuery[UserTable]

  private type UserQuery = Query[UserTable, User, Seq]
  implicit class FilterHelper[UserQuery](q: UserQuery) {
    def ifThen[B](cond: Boolean) (f: UserQuery => UserQuery ): UserQuery = {
      if (cond) f(q) else q
    }
  }

  def userSeq(nameOpt: Option[String] = None): Future[Seq[User]] = {
    val query = userTable
      .sortBy( _.name.asc )
      .ifThen(nameOpt.isDefined) { _.filter( _.name   ===   nameOpt.get ) }
      .result

    db.run(query)
  }

  def userStream(nameOpt: Option[String] = None): DatabasePublisher[User] = {
    val query = userTable
      .sortBy( _.name.asc )
      .ifThen(nameOpt.isDefined) { _.filter( _.name   ===   nameOpt.get ) }
      .result

    db.stream(query)
  }

  def count(): Future[Int] = {
    db.run{ userTable.length.result }
  }

  def create(numRows: Int): Future[Unit] = {
    db.run (
      DBIO.seq(
        userTable ++= (1 to numRows).map(i => User("Logan_" + i, i + "00"))
      )
    )
  }

  def deleteAll(): Future[Int] = {
    db.run(userTable.delete)
  }


  private class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def name           = column[String]("name")
    def documentNumber = column[String]("documentNumber")
    def enabled        = column[Boolean]("enabled")
    def id             = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def *              = (name , documentNumber, enabled, id) <> ((User.apply _).tupled, User.unapply)
  }

}
