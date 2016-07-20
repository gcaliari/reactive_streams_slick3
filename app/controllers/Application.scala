package controllers

import com.google.inject.Inject
import models.UserRepository
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._

class Application @Inject()(userRepository: UserRepository) extends Controller {

  def index = Action.async {
    userRepository.count().map{ count =>
      Ok(views.html.index("Welcome! First created some users, then stream the results", count / 1000000))
    }
  }
}
