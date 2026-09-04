import java.nio.file.{Files, Paths}

import org.scalatestplus.play._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
class ApplicationSpec extends PlaySpec with GuiceOneAppPerTest {

  "Routes" should {

    "send 404 on a bad request" in {
      route(app, FakeRequest(GET, "/boum")).map(status(_)) mustBe Some(NOT_FOUND)
    }

  }

  "HomeController" should {

    "render the index page" in {
      val home = route(app, FakeRequest(GET, "/")).get

      status(home) mustBe OK
      contentType(home) mustBe Some("text/html")
      contentAsString(home) must include("Your new application is ready.")
    }

  }

  "Swagger spec" should {

    "emit enumeratum Color as a reusable schema" in {
      val swaggerPath = Paths.get("target/swagger/swagger.json")
      Files.exists(swaggerPath) mustBe true

      val json = Json.parse(Files.readAllBytes(swaggerPath))
      (json \ "definitions" \ "models.Paint" \ "properties" \ "color" \ "$ref").as[String] mustBe
        "#/definitions/models.Color"
      (json \ "definitions" \ "models.Paint" \ "properties" \ "accent" \ "$ref").as[String] mustBe
        "#/definitions/models.Color"
      (json \ "definitions" \ "models.Color" \ "type").as[String] mustBe "string"
      (json \ "definitions" \ "models.Color" \ "enum").as[Seq[String]] mustBe Seq("RED", "GREEN", "PUCE")
    }

  }

  "CountController" should {

    "return an increasing count" in {
      contentAsString(route(app, FakeRequest(GET, "/math/count")).get) mustBe "0"
      contentAsString(route(app, FakeRequest(GET, "/math/count")).get) mustBe "1"
      contentAsString(route(app, FakeRequest(GET, "/math/count")).get) mustBe "2"
    }

  }

}
