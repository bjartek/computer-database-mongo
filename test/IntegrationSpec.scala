package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

import org.fluentlenium.core.filter.FilterConstructor._

class IntegrationSpec extends Specification {

  import models._
  import com.mongodb.casbah.Imports._

  def mongoTestDatabase() = {
    Map("mongo.default.db" -> "computer-database-test")
  }

  val macintoshId = new ObjectId("4f7e12bf7f25471356f51e39")
  val appleCompanyId = new ObjectId("4f7dc7c47f25471356f51366")

  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str

  def dateFromString(str:String) = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(str)

  step {
      running(FakeApplication(additionalConfiguration = mongoTestDatabase())) {
        Computer.remove(MongoDBObject.empty)
        Company.remove(MongoDBObject.empty)

        val apple = Company(appleCompanyId, "Apple Inc.")
        Company.insert(apple)

        val mac = Computer(macintoshId, "Macintosh", Some(dateFromString("1984-01-24")), None, Some(appleCompanyId))
        Computer.insert(mac)
        Computer.insert(Computer(name = "LISA", companyId = Some(appleCompanyId)))
        Computer.insert(Computer(name = "Commodore 64"))
        Computer.insert(Computer(name = "Commodore 128"))
      }
  }


  "Application" should {
    "work from within a browser" in {
      running(TestServer(3333, FakeApplication(additionalConfiguration = mongoTestDatabase())), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")

        browser.$("header h1").first.getText must equalTo("Play 2.0 sample application — Computer database")
        browser.$("section h1").first.getText must equalTo("4 computers found")

        browser.$("#pagination li.current").first.getText must equalTo("Displaying 1 to 4 of 4")

        browser.$("#searchbox").text("Commodore")
        browser.$("#searchsubmit").click()

        browser.$("section h1").first.getText must equalTo("2 computers found")
        browser.$("a", withText("Commodore 64")).click()

        browser.$("section h1").first.getText must equalTo("Edit computer")

        browser.$("#discontinued").text("xxx")
        browser.$("input.primary").click()

        browser.$("div.error").size must equalTo(1)
        browser.$("div.error label").first.getText must equalTo("Discontinued date")

        browser.$("#discontinued").text("")
        browser.$("input.primary").click()

        browser.$("section h1").first.getText must equalTo("4 computers found")
        browser.$(".alert-message").first.getText must equalTo("Done! Computer Commodore 64 has been updated")

        browser.$("#searchbox").text("Commodore")
        browser.$("#searchsubmit").click()

        browser.$("a", withText("Commodore 64")).click()
        browser.$("input.danger").click()

        browser.$("section h1").first.getText must equalTo("3 computers found")
        browser.$(".alert-message").first.getText must equalTo("Done! Computer has been deleted")

        browser.$("#searchbox").text("Commodore")
        browser.$("#searchsubmit").click()

        browser.$("section h1").first.getText must equalTo("One computer found")
      }
    }
  }
}
