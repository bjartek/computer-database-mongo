package models

import play.api.Play.current
import com.novus.salat._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import com.novus.salat.annotations._
import se.radley.plugin.salat._
import models.mongoContext._

case class Company(
  @Key("_id") id: ObjectId = new ObjectId, 
  name:String
)

object Company extends ModelCompanion[Company, ObjectId] {
  val collection = mongoCollection("company")
  val dao = new SalatDAO[Company, ObjectId](collection = collection) {}

  def options: Seq[(String,String)] = {
    find(MongoDBObject.empty).map(it => (it.id.toString, it.name)).toSeq
  }

}
