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
      val home = route(FakeRequest(GET, "/create")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("DB Created and populated!")

      Await.result(User.count(), 1 second) must equalTo(99999)
    }
  }
}
