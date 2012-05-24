package controllers

import play.api._
import libs.concurrent.Promise
import libs.json.{JsArray, JsString, JsObject, Reads}
import play.api.mvc._
import org.joda.time.DateTime
import play.api.libs.json.Json._
import model._
import org.joda.time.format.ISODateTimeFormat

object Application extends Controller {
  implicit val reads = Reads
  import AbstractStory.GuImporterJsonWriter
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def dt = DateTime.now.minusHours(24)
  def allStories : Promise[Seq[AbstractStory]] = for {
    gustory <- GUStoryCollector.storiesSince(dt)
    ftstory <- FTStoryCollector.storiesSince(dt)
    nytstory <- NYTNewsStreamStoryCollector.storiesSince(dt)
  } yield gustory ++ ftstory ++ nytstory

  def sortedStories = allStories map { stories => stories.sortWith((a,b) => a.publicationDate.isBefore(b.publicationDate))}

  def stats = Action {
    Async {
      sortedStories map (x => Ok(toJson(x)))
    }
  }

  def stats_ft = Action { Async {
    FTStoryCollector.storiesSince(DateTime.now.minusHours(24)) map (x => Ok(toJson(x)))
  }}

  def stats_gu = Action { Async {
    GUStoryCollector.storiesSince(DateTime.now.minusHours(24)) map (x => Ok(toJson(x)))
  }}

  def stats_nyt_news = Action { Async {
    NYTNewsStreamStoryCollector.storiesSince(DateTime.now.minusHours(24)) map (x => Ok(toJson(x)))
  }}

  def stats_nyt_search = Action { Async {
    NYTSearchStoryCollector.storiesSince(DateTime.now.minusHours(24)) map (x => Ok(toJson(x)))
  }}

  def stats_wp = Action { Async {
    WPStoryCollector.storiesSince(DateTime.now.minusHours(24)) map (x => Ok(toJson(x)))
  }}

  def colour_for(source: String):String = source match {
    case "FT" => "pink"
    case "GU" => "blue"
    case "NYT" => "gray"
    case _ => "black"
  }

  def to_timeline_json(events: Seq[AbstractStory]) = {
    lazy val timeFormatter = ISODateTimeFormat.dateTimeNoMillis.withZoneUTC()

    JsObject(
      ("dateTimeFormat" -> JsString("Gregorian")) ::
      ("events") -> JsArray(
      events.map { o => JsObject(
        ("title" -> JsString(o.headline))
          :: ("start") -> JsString(timeFormatter.print(o.publicationDate))
          :: ("color") -> JsString(colour_for(o.source))
          :: ("description") -> JsString("<h1>%s</h1><p>%s</p><p><b>tags:</b>%s</p>".format(
          o.source,o.headline,o.tags.mkString(", ")))
          :: Nil)
      }
      ) :: Nil
    )
  }

  def stories_timeline_js = Action { Async {
    allStories map (x => Ok(to_timeline_json(x)))
  }}
}