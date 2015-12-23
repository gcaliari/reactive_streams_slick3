package controllers

import java.nio.charset.StandardCharsets.UTF_16LE

import models.User
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import play.api.mvc._
import slick.backend.DatabasePublisher

import scala.concurrent.Future

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Welcome! First created the DB, then stream the results"))
  }
}
