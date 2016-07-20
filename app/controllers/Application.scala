package controllers

import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Welcome! First created the DB, then stream the results"))
  }
}
