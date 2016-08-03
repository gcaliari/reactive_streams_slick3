package controllers

import java.nio.charset.StandardCharsets.UTF_16LE

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.google.inject.Inject
import models.{User, UserRepository}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsArray, Json}
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
      val userPublisher: DatabasePublisher[String] = userRepository.userStream().mapResult(u => Json.toJson(u).toString)
      val userSource: Source[String, NotUsed] = Source.fromPublisher(userPublisher)
      Ok.chunked(userSource.intersperse("[", ",", "]")).as(s"text/json; charset=$charset")
        .withHeaders(CONTENT_ENCODING -> charset.name)
        .withHeaders(CONTENT_DISPOSITION -> s"attachment; filename=$filename")
    }
  }

}
