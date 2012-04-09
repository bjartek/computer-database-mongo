package models

import com.novus.salat.Context
import play.api.Play
import play.api.Play._

package object SalatImports {


  import com.mongodb.casbah.Imports._

  def mongoCollection(collectionName: String) : MongoCollection = {
    val dbName = Play.configuration.getString("mongo.dbName").getOrElse("computer-database")

    val host = Play.configuration.getString("mongo.host")
    val port = Play.configuration.getInt("mongo.port")

    (host, port) match { 
      case (Some(host), Some(port)) => MongoConnection(host, port)(dbName)(collectionName)
      case (Some(host), None) => MongoConnection(host)(dbName)(collectionName)
      case _ => MongoConnection()(dbName)(collectionName)
    }

  }

  implicit val ctx = {
    val c = new Context {
      val name = "play-context"
    }

    c.registerClassLoader(Play.classloader)

    c
  }

}

