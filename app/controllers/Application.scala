package controllers

import play.api._
import libs.json.Reads
import play.api.mvc._
import org.joda.time.DateTime
import play.api.libs.json.Json._
import model.{AbstractStory, GuImporter}

object Application extends Controller {
  implicit val reads = Reads
  import AbstractStory.GuImporterJsonWriter
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def stats_gu = Action {

    Ok(toJson(GuImporter.storiesSince(DateTime.now.minusHours(24)).await.get))
  }
  
}