package controllers

import play.api._
import libs.concurrent.Promise
import libs.json.Reads
import play.api.mvc._
import org.joda.time.DateTime
import play.api.libs.json.Json._
import model._

object Application extends Controller {
  implicit val reads = Reads
  import AbstractStory.GuImporterJsonWriter
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def allStories = {
    val dt = DateTime.now.minusHours(24)
    val allStories : Promise[Seq[AbstractStory]] = for {
      gustory <- GUStoryCollector.storiesSince(dt)
      ftstory <- FTStoryCollector.storiesSince(dt)
      nytstory <- NYTSearchStoryCollector.storiesSince(dt)
    } yield gustory ++ ftstory ++ nytstory

    allStories
  }

  def stats = Action {

    val sortedStories = allStories map { stories => stories.sortWith((a,b) => a.publicationDate.isBefore(b.publicationDate))}
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

//  def allWords = Action { Async {
//
//  }}
}