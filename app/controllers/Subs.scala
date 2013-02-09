package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.mvc.RequestHeader
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._

import models._
import models.auth._
import views._

object Subs extends AuthController {

  val itemForm = Form(
    mapping(
      "title" -> text,
      "postedBy" -> longNumber,
      "postedTo" -> longNumber,
      "link" -> optional(text),
      "content" -> text
    )((title, postedBy, postedTo, link, content) => Item(None, title, postedBy, postedTo, 0, link, content))
     ((item: Item) => Some((item.title, item.postedBy, item.postedTo, item.link, item.content)))
  )
      
  
  def index(subName: String) = MaybeAuthenticated { implicit maybeUser => implicit request =>
    Sub.findByName(subName) match { 
      case Some(sub) =>
        Ok(views.html.sub(
          sub,
          Sub.frontpage(sub.id.get).get
        ))
      case None => Ok(views.html.subNotFound(subName))
    }
  }

  def createItem = MaybeAuthenticated { implicit maybeUser => implicit request =>
    (for {
      item <- (itemForm.bindFromRequest.value  toRight "Could not bind form to value").right
      subId <- (Item.create(item)              toRight "Could not get unique ID for new item").right
      sub <- (Sub.findById(subId)              toRight "Could not find sub by ID: " + subId).right
      items <- (Sub.frontpage(subId)           toRight "Could not get frontpage items for sub by ID: " + subId).right
    } yield Ok(views.html.sub(sub, items))) fold (
      error => BadRequest(error),
      ok => ok
    )
  }


}
