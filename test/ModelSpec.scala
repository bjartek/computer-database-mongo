package test

import org.specs2.mutable._


import play.api._
import play.api.test._
import play.api.test.Helpers._

class ModelSpec extends Specification {

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

        val mac = Computer(macintoshId, "Macintosh", Some(dateFromString("1984-01-24")), None, Some(appleCompanyId))
        Computer.insert(mac)
      }
  }

  "Computer model" should {
    "be retrieved by id" in {
      running(FakeApplication(additionalConfiguration = mongoTestDatabase())) {
        val Some(macintosh) = Computer.findOneByID(macintoshId)
        macintosh.name must endWith("Macintosh")
        macintosh.introduced must beSome.which(dateIs(_, "1984-01-24"))  
      }
    }

    "be listed along its companies" in {
      running(FakeApplication(additionalConfiguration = mongoTestDatabase())) {
        val computers = Computer.list()
        computers.total must equalTo(1)
        computers.items must have length(1)
      }
    }

    "be updated if needed" in {
      running(FakeApplication(additionalConfiguration = mongoTestDatabase())) {
        Computer.save(Computer(id=macintoshId, name="The Macintosh", introduced=None, discontinued=None, companyId=Some(appleCompanyId)))
        val Some(macintosh) = Computer.findOneByID(macintoshId)
        macintosh.name must equalTo("The Macintosh")
        macintosh.introduced must beNone
      }
    }
  }
}
