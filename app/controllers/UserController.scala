package controllers

import java.nio.charset.StandardCharsets.UTF_16LE

import akka.stream.scaladsl.Source
import models.User
import org.joda.time.DateTime
import play.api.http.HttpEntity
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.mvc._
import slick.backend.DatabasePublisher

import scala.concurrent.Future

class UserController extends Controller {

  val filename = "reactive_streams.csv"
  val charset = UTF_16LE

  def create(rows: Int)= Action.async {
    User.create(rows).map { r =>
      Ok(views.html.index(s"+3M users created on ${DateTime.now}!"))
    }
  }

  def deleteAll() = Action.async {
    User.deleteAll().map { r =>
      Ok(views.html.index("All users deleted!"))
    }
  }

  def csvResponse = Action.async {
    User.userSeq().map { users =>
      Ok(users.mkString(";")).as(s"text/csv; charset=$charset")
        .withHeaders(CONTENT_ENCODING -> charset.name)
        .withHeaders(CONTENT_DISPOSITION -> s"attachment; filename=$filename")
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
