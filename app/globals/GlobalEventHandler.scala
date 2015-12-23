package globals

import akka.actor.{Actor, Props}
import models.User
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.WithFilters
import play.api.{GlobalSettings, Application, Logger}

import scala.concurrent.Await
import scala.concurrent.duration._

object GlobalEventHandler extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Starting Application")
    Await.result(User.createDb(), 10 seconds)

    super.onStart(app)
  }


  override def onStop(app : play.api.Application) = {
    Logger.info("Stopping Application")
    Await.result(User.destroyDb(), 10 seconds)
    super.onStop(app)
  }


}
