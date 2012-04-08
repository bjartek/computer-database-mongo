import play.api._
import models._
import com.mongodb.casbah.Imports._

object Global extends GlobalSettings {

  override def onStart(app: Application) {

    val size = CompanyDAO.count(MongoDBObject.empty)
    if(size == 0) {
      insertTestData() 
    }

  }

  def insertTestData() = {
    val c = createCompanies();
    createComputers(c)
  }

  def createCompanies() = {
    Logger.info("Creating new companies since none existed in mongo")
    val companies = play.Play.application().getFile("data/companies.csv");
    val lines = scala.io.Source.fromFile(companies).getLines
    val savedCompanies = lines.map{line => 
      val id = CompanyDAO.insert(Company(name = line))
      line -> id
    }

    savedCompanies.toMap
  }

  def createComputers(company: Map[String, Option[ObjectId]]) = {
    Logger.info("Creating new computers since none existed in mongo")
    val companies = play.Play.application().getFile("data/computers.csv");
    val lines = scala.io.Source.fromFile(companies).getLines
    lines.foreach{line => 
      val elements = line.split(",") 

      val computer = Computer(
        name = elements(0),
        introduced =   if(elements(1) == "null") None else Some(dateFromString(elements(1))),
        discontinued = if(elements(2) == "null") None else Some(dateFromString(elements(2))),
        companyId =    if(elements(3) == "null") None else company(elements(3))
      )

      ComputerDAO.insert(computer)
    }
  }

  def dateFromString(str:String) = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(str)
}
