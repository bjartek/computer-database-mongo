package models

import java.util.{Date}
import play.api.Play.current
import com.novus.salat._
import com.novus.salat.dao._
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import se.radley.plugin.salat._
import com.novus.salat.global._

case class Computer(
  @Key("_id") id: ObjectId = new ObjectId, 
  name: String, 
  introduced: Option[Date] = None,
  discontinued: Option[Date] = None, 
  @Key("company_id") companyId: Option[ObjectId] = None 
)

object Computer extends ModelCompanion[Computer, ObjectId] {
  val collection = mongoCollection("computers")
  val dao = new SalatDAO[Computer, ObjectId](collection = collection) {}

  val columns = List("dummy", "_id", "name", "introduced", "discontinued", "company_id")

  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = ""): Page[(Computer, Option[Company])] = {

    val where = if(filter == "") MongoDBObject.empty else MongoDBObject("name" ->(""".*"""+filter+""".*""").r)
    val ascDesc = if(orderBy > 0) 1 else -1
    val order = MongoDBObject(columns(orderBy.abs) -> ascDesc)

    val totalRows = count(where);
    val offset = pageSize * page
    val computer = find(where).sort(order).limit(pageSize).skip(offset).toSeq

    val computers = computer.map{ c => 
      val company = c.companyId.flatMap(id => Company.findOneById(id))
      (c, company)
    }
    Page(computers, page, offset, totalRows)
  }
}

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}


