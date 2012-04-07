package models

import play.api._

import java.util.{Date}

import com.mongodb.casbah.Imports._

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.dao._
import com.novus.salat.annotations._


/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}


case class Company(@Key("_id") id: ObjectId = new ObjectId, name: String)
case class Computer(@Key("_id") id: ObjectId = new ObjectId, name: String, introduced: Option[Date], discontinued: Option[Date], @Key("company_id") companyId: Option[ObjectId]) 


object Computer {
  def fromForm(id:ObjectId, name:String, introduced:Option[Date], discontinued:Option[Date], company: Option[String]) :Computer ={
    Computer(id, name, introduced, discontinued, company.map(c => new ObjectId(c))) 
  }

  def toForm(computer: Computer) = {
    Some((computer.id, computer.name, computer.introduced, computer.discontinued, computer.companyId.map(c => c.toString)))
  }

}

object CompanyDAO extends SalatDAO[Company, ObjectId](collection = MongoConnection()("computer-database")("companies"))  {

  
  def options: Seq[(String,String)] = {
    find(MongoDBObject.empty).map(it => (it.id.toString, it.name)).toSeq
  }
}


object ComputerDAO extends SalatDAO[Computer, ObjectId](collection = MongoConnection()("computer-database")("computers")) {

  val columns = List("dummy", "_id", "name", "introduced", "discontinued", "company_id")

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = ""): Page[(Computer, Option[Company])] = {

    val where = if(filter == "") MongoDBObject.empty else MongoDBObject("name" ->(""".*"""+filter+""".*""").r)
    val ascDesc = if(orderBy > 0) 1 else -1
    val order = MongoDBObject(columns(orderBy.abs) -> ascDesc)

    val totalRows = count(where);
    val offset = pageSize * page
    val computer = find(where).sort(order).limit(pageSize).skip(offset).toSeq

    computer.foreach(it => Logger.info(it.toString))

    val computers = computer.map{ c => 
      val company = c.companyId.flatMap(id => CompanyDAO.findOneByID(id))
      (c, company)
    }
    Page(computers, page, offset, totalRows)
  }
}


