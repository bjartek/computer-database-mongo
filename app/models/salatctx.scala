package models

import com.novus.salat.Context
import play.api.Play
import play.api.Play._

package object salatctx {

  implicit val ctx = {
    val c = new Context {
      val name = "play-context"
    }

    c.registerClassLoader(Play.classloader)

    c
  }

}

