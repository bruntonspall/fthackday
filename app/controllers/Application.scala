package controllers

import play.api._
import libs.concurrent.Promise
import libs.json.Reads
import play.api.mvc._
import org.joda.time.DateTime
import play.api.libs.json.Json._
import model.{FTStoryCollector, AbstractStory, GuImporter, NYTStoryCollector}

object Application extends Controller {
  implicit val reads = Reads
  import AbstractStory.GuImporterJsonWriter
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def stats = Action {
    val dt = DateTime.now.minusHours(24)
    val allStories : Promise[Seq[AbstractStory]] = for {
      gustory <- GuImporter.storiesSince(dt)
      ftstory <- FTStoryCollector.storiesSince(dt)
      nytstory <- NYTStoryCollector.storiesSince(dt)
    } yield gustory ++ ftstory ++ nytstory

    val sortedStories = allStories map { stories => stories.sortWith((a,b) => a.publicationDate.isBefore(b.publicationDate))}
//        val stories = GuImporter.storiesSince(dt)
    Ok(toJson(sortedStories.await.get))
  }

  def stats_ft = Action {
    Ok(toJson(FTStoryCollector.storiesSince(DateTime.now.minusHours(24)).await.get))
  }

  def stats_gu = Action {
    Ok(toJson(GuImporter.storiesSince(DateTime.now.minusHours(24)).await.get))
  }

  def stats_nyt = Action {

    Ok(toJson(NYTStoryCollector.storiesSince(DateTime.now.minusHours(24)).await.get))
  }
  
}