package controllers

import java.nio.charset.StandardCharsets.UTF_16LE

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.google.inject.Inject
import models.{User, UserRepository}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.mvc._
import slick.backend.DatabasePublisher

import scala.concurrent.Future

class UserController @Inject()(userRepository: UserRepository) extends Controller {

  val filename = "reactive_streams.json"
  val charset = UTF_16LE

  def createOneMillion()= {
    val oneMillion: Int = 1000000
    create(oneMillion)
  }

  def create(rows: Int)= Action.async {
    userRepository.create(rows).map { r =>
      Redirect(routes.Application.index())
    }
  }

  def deleteAll() = Action.async {
    userRepository.deleteAll().map { r =>
      Redirect(routes.Application.index())
    }
  }

  def csvResponse = Action.async {
    implicit val userFormat = Json.format[User]
    userRepository.userSeq().map { users =>
      Ok(new JsArray(users.map(u => Json.toJson(u)))).as(s"text/json; charset=$charset")
        .withHeaders(CONTENT_ENCODING -> charset.name)
        .withHeaders(CONTENT_DISPOSITION -> s"attachment; filename=$filename")
    }
  }

  def csvStream = Action.async {
    implicit val userFormat = Json.format[User]
    Future {
      val userPublisher: DatabasePublisher[JsValue] = userRepository.userStream().mapResult(u => Json.toJson(u))
      val userSource: Source[JsValue, NotUsed] = Source.fromPublisher(userPublisher)

      Ok.chunked(userSource).as(s"text/json; charset=$charset")
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
