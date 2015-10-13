package controllers

import java.nio.charset.StandardCharsets.UTF_16LE

import models.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import slick.backend.DatabasePublisher

import scala.concurrent.Future

class Application extends Controller {

  val filename = "reactive_streams.csv"
  val charset = UTF_16LE

  def index = Action {
    Ok(views.html.index("Welcome! First created the DB, then stream the results"))
  }

  def createAndPopulateDb = Action.async {
    User.createAndPopulateDb().map { r =>
      Ok(views.html.index("DB Created and populated!"))
    }
  }

  def deleteDb = Action.async {
    User.deleteDb().map { r =>
      Ok(views.html.index("DB Deleted!"))
    }
  }

  def csvStream = Action.async {
    Future {
      val userStream: DatabasePublisher[User] = User.userStream()
      val userEnum: Enumerator[User] = play.api.libs.streams.Streams.publisherToEnumerator(userStream)
      val byteArrayEnumerator: Enumerator[Array[Byte]] = Enumerator(csvHeader.getBytes(charset)) >>> userEnum.map(user => userToCsv(user).getBytes(charset))

      Ok.chunked[Array[Byte]](byteArrayEnumerator).as(s"text/csv; charset=$charset")
        .withHeaders(CONTENT_ENCODING -> charset.name)
        .withHeaders(CONTENT_DISPOSITION -> s"attachment; filename=$filename")
    }
  }


  private val csvHeader =
    """sep=,
      |"Id","Name","Document","Enabled"""".stripMargin


  private def userToCsv(user: User): String = {
    "\n" + List(user.id.getOrElse("--"), user.name, user.documentNumber, user.enabled).map("\"%s\"".format(_)).mkString(",")
  }
  
}
