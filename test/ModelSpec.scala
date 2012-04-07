package test

import org.specs2.mutable._


import play.api.test._
import play.api.test.Helpers._

class ModelSpec extends Specification {
  
  import models._
  import com.mongodb.casbah.Imports._

  // -- Date helpers
  
  def dateIs(date: java.util.Date, str: String) = new java.text.SimpleDateFormat("yyyy-MM-dd").format(date) == str

  val macintoshId = new ObjectId("4f7e12bf7f25471356f51e39")
  val appleCompanyId = new ObjectId("4f7dc7c47f25471356f51366")
  
  //TODO: need some way to create an test database here. 
  // --
  
  "Computer model" should {
    
    "be retrieved by id" in {
      running(FakeApplication()) {
        
        val Some(macintosh) = ComputerDAO.findOneByID(macintoshId)
      
        macintosh.name must endWith("Macintosh")
        macintosh.introduced must beSome.which(dateIs(_, "1984-01-24"))  
        
      }
    }
    
    "be listed along its companies" in {
      running(FakeApplication()) {
        
        val computers = ComputerDAO.list()

        computers.total must equalTo(552)
        computers.items must have length(10)

      }
    }
    
    "be updated if needed" in {
      running(FakeApplication()) {
        
        ComputerDAO.save(MongoComputer(id=macintoshId, name="The Macintosh", introduced=None, discontinued=None, companyId=Some(appleCompanyId)))
        
        val Some(macintosh) = ComputerDAO.findOneByID(macintoshId)
        
        macintosh.name must equalTo("The Macintosh")
        macintosh.introduced must beNone
        
      }
    }
    
  }
  
}
