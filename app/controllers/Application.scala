package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import org.bson.types.ObjectId

import play.api.Play.current
import se.radley.plugin.salat._
import se.radley.plugin.salat.Formats._
import com.novus.salat._

import com.mongodb.casbah.Imports._
import views._
import models._

/**
 * Manage a database of computers
 */
object Application extends Controller { 

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Application.list(0, 2, ""))
  
  /**
   * Describe the computer form (used in both edit and create screens).
   */ 
  def computerForm(id: ObjectId = new ObjectId) = Form(
    mapping(
      "id" -> ignored(id),
      "name" -> nonEmptyText,
      "introduced" -> optional(date("yyyy-MM-dd")),
      "discontinued" -> optional(date("yyyy-MM-dd")),
      "company" -> optional(of[ObjectId])
    )(Computer.apply)(Computer.unapply)
  )
  
  // -- Actions

  /**
   * Handle default path requests, redirect to computers list
   */  
  def index = Action { Home }
  
  /**
   * Display the paginated list of computers.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on computer names
   */
  def list(page: Int, orderBy: Int, filter: String) = Action { implicit request =>
    Ok(html.list(
      Computer.list(page = page, orderBy = orderBy, filter = filter),
      orderBy, filter
    ))
  }
  
  /**
   * Display the 'edit form' of a existing Computer.
   *
   * @param id Id of the computer to edit
   */
  def edit(id: ObjectId) = Action { 
    Computer.findOneById(id).map { computer =>
      Ok(html.editForm(id, computerForm(id).fill(computer), Company.options))
    }.getOrElse(NotFound)
  }
  
  /**
   * Handle the 'edit form' submission 
   *
   * @param id Id of the computer to edit
   */
  def update(id: ObjectId) = Action { implicit request =>
    computerForm(id).bindFromRequest.fold(
      formWithErrors => BadRequest(html.editForm(id, formWithErrors, Company.options)),
      computer => {
        Computer.save(computer.copy(id = id))
        Home.flashing("success" -> "Computer %s has been updated".format(computer.name))
      }
    )
  }
  
  /**
   * Display the 'new computer form'.
   */
  def create = Action {
    Ok(html.createForm(computerForm(), Company.options))
  }
  
  /**
   * Handle the 'new computer form' submission.
   */
  def save = Action { implicit request =>
    computerForm().bindFromRequest.fold(
      formWithErrors => BadRequest(html.createForm(formWithErrors, Company.options)),
      computer => {
        Computer.insert(computer)
        Home.flashing("success" -> "Computer %s has been created".format(computer.name))
      }
    )
  }
  
  /**
   * Handle computer deletion.
   */
  def delete(id: ObjectId) = Action { 
    Computer.remove(MongoDBObject("_id" -> id))
    Home.flashing("success" -> "Computer has been deleted")
  }

}
