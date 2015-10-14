import models.User
import org.junit.runner._
import org.specs2.runner._
import play.api.test._

import scala.concurrent.Await
import scala.concurrent.duration._


@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends PlaySpecification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

    "render the index page" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Welcome! First created the DB, then stream the results")
    }

    "create and populate database" in new WithApplication {
      val createPage = route(FakeRequest(GET, "/create")).get

      status(createPage) must equalTo(OK)
      contentType(createPage) must beSome.which(_ == "text/html")
      contentAsString(createPage) must contain ("DB Created and populated!")

      Await.result(User.count(), 2 seconds) must equalTo(99999)
    }

    "send stream of data" in new WithApplication {
      val csvFile = route(FakeRequest(GET, "/stream")).get

      status(csvFile) must equalTo(OK)
      contentType(csvFile) must beSome.which(_ == "text/csv")
      contentAsString(csvFile) must contain (""""Id","Name","Document","Enabled"""".stripMargin)
      contentAsString(csvFile) must contain ("Logan_")
    }

    "delete data from database" in new WithApplication {
      val deletePage = route(FakeRequest(GET, "/delete")).get

      status(deletePage) must equalTo(OK)
      contentType(deletePage) must beSome.which(_ == "text/html")
      contentAsString(deletePage) must contain ("DB Deleted!")
    }

  }
}
