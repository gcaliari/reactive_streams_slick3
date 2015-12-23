package controllers

import models.User
import org.junit.runner._
import org.specs2.runner._
import play.api.test._

import scala.concurrent.Await
import scala.concurrent.duration._


@RunWith(classOf[JUnitRunner])
class UserControllerSpec extends PlaySpecification {

  "UserController" should {

    "create and populate database" in new WithApplication {
      val createPage = route(FakeRequest(GET, "/user/create?rows=1000")).get

      status(createPage) must equalTo(OK)
      contentType(createPage) must beSome.which(_ == "text/html")
      contentAsString(createPage) must contain ("+1000 users created")

      Await.result(User.count(), 2 seconds) must greaterThanOrEqualTo(1000)
    }

    "send stream of data" in new WithApplication {
      val createPage = route(FakeRequest(GET, "/user/create?rows=1000")).get
      status(createPage) must equalTo(OK)

      val csvFile = route(FakeRequest(GET, "/user/stream")).get
      status(csvFile) must equalTo(OK)
      contentType(csvFile) must beSome.which(_ == "text/csv")
      contentAsString(csvFile) must contain (""""Id","Name","Document","Enabled"""".stripMargin)
      contentAsString(csvFile) must contain ("Logan_")
    }

    "delete data from database" in new WithApplication {
      val deletePage = route(FakeRequest(GET, "/user/deleteAll")).get

      status(deletePage) must equalTo(OK)
      contentType(deletePage) must beSome.which(_ == "text/html")
      contentAsString(deletePage) must contain ("All users deleted!")
    }

  }
}
