package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends Specification {
  import models._
  import com.mongodb.casbah.Imports._

  def mongoTestDatabase() = {
    Map("mongo.url" -> "computer-database-test")
  }

  val macintoshId = new ObjectId("4f7e12bf7f25471356f51e39")
  val appleCompanyId = new ObjectId("4f7dc7c47f25471356f51366")

  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str

  def dateFromString(str:String) = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(str)

  step {
      running(FakeApplication(additionalConfiguration = mongoTestDatabase())) {
        ComputerDAO.remove(MongoDBObject.empty)
        CompanyDAO.remove(MongoDBObject.empty)

        val apple = Company(appleCompanyId, "Apple Inc.")
        CompanyDAO.insert(apple)

        val mac = Computer(macintoshId, "Macintosh", Some(dateFromString("1984-01-24")), None, Some(appleCompanyId))
        ComputerDAO.insert(mac)
        ComputerDAO.insert(Computer(name = "LISA", companyId = Some(appleCompanyId)))
        ComputerDAO.insert(Computer(name = "Commodore 64"))
        ComputerDAO.insert(Computer(name = "Commodore 128"))
      }
  }

  "Application" should {

    "redirect to the computer list on /" in {
      val result = controllers.Application.index(FakeRequest())
      status(result) must equalTo(SEE_OTHER)
      redirectLocation(result) must beSome.which(_ == "/computers")
    }

    "list computers on the the first page" in {
      running(FakeApplication(additionalConfiguration = mongoTestDatabase())) {
        val result = controllers.Application.list(0, 2, "")(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain("4 computers found")
      }
    }

    "filter computer by name" in {
      running(FakeApplication(additionalConfiguration = mongoTestDatabase())) {
        val result = controllers.Application.list(0, 2, "Commodore")(FakeRequest())

        status(result) must equalTo(OK)
        contentAsString(result) must contain("2 computers found")
      }
    }
    "create new computer" in {
      running(FakeApplication(additionalConfiguration = mongoTestDatabase())) {
        val badResult = controllers.Application.save(FakeRequest())
        status(badResult) must equalTo(BAD_REQUEST)
        val badDateFormat = controllers.Application.save(
          FakeRequest().withFormUrlEncodedBody("name" -> "FooBar", "introduced" -> "badbadbad", "company" -> "4f7dc7c47f25471356f51366")
        )
        status(badDateFormat) must equalTo(BAD_REQUEST)
        contentAsString(badDateFormat) must contain("""<option value="4f7dc7c47f25471356f51366" selected>Apple Inc.</option>""")
        contentAsString(badDateFormat) must contain("""<input type="text" id="introduced" name="introduced" value="badbadbad" >""")
        contentAsString(badDateFormat) must contain("""<input type="text" id="name" name="name" value="FooBar" >""")
        val result = controllers.Application.save(
          FakeRequest().withFormUrlEncodedBody("name" -> "FooBar", "introduced" -> "2011-12-24", "company" -> "4f7dc7c47f25471356f51366")
        )
        status(result) must equalTo(SEE_OTHER)
        redirectLocation(result) must beSome.which(_ == "/computers")
        flash(result).get("success") must beSome.which(_ == "Computer FooBar has been created")
        val list = controllers.Application.list(0, 2, "FooBar")(FakeRequest())

        status(list) must equalTo(OK)
        contentAsString(list) must contain("One computer found")
      }
    }
  }
}
