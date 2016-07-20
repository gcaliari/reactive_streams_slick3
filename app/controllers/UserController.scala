package controllers

import java.nio.charset.StandardCharsets.UTF_16LE

import akka.stream.scaladsl.Source
import com.google.inject.Inject
import models.{User, UserRepository}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.Future

class UserController @Inject()(userRepository: UserRepository) extends Controller {

  val filename = "reactive_streams.csv"
  val charset = UTF_16LE

  def createOneMillion()= {
    val oneMillion: Int = 1000000
    create(oneMillion)
  }

  def create(rows: Int)= Action.async {
    userRepository.create(rows).map { r =>

      Redirect(routes.Application.index())//s"+1M users created on ${DateTime.now}!"))
    }
  }

  def deleteAll() = Action.async {
    userRepository.deleteAll().map { r =>
      Ok(views.html.index("All users deleted!"))
    }
  }

  def csvResponse = Action.async {
    userRepository.userSeq().map { users =>
      Ok(users.mkString(";")).as(s"text/csv; charset=$charset")
        .withHeaders(CONTENT_ENCODING -> charset.name)
        .withHeaders(CONTENT_DISPOSITION -> s"attachment; filename=$filename")
    }
  }

  def csvStream = Action.async {
    Future {
      implicit val userFormat = Json.format[User]

      val userSource = Source.fromPublisher(userRepository.userStream().mapResult(u => Json.toJson(u)))
      Ok.chunked(userSource).as(s"text/csv; charset=$charset")
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
