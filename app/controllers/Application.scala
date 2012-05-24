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
  def timeline = Action {
    Ok(views.html.timeline())
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


  val stopwords = """-|i|me|my|myself|we|us|our|ours|ourselves|you|your|yours|yourself|yourselves|he|him|his|himself|she|her|hers|herself|it|its|itself|they|them|their|theirs|themselves|what|which|who|whom|whose|this|that|these|those|am|is|are|was|were|be|been|being|have|has|had|having|do|does|did|doing|will|would|should|can|could|ought|i'm|you're|he's|she's|it's|we're|they're|i've|you've|we've|they've|i'd|you'd|he'd|she'd|we'd|they'd|i'll|you'll|he'll|she'll|we'll|they'll|isn't|aren't|wasn't|weren't|hasn't|haven't|hadn't|doesn't|don't|didn't|won't|wouldn't|shan't|shouldn't|can't|cannot|couldn't|mustn't|let's|that's|who's|what's|here's|there's|when's|where's|why's|how's|a|an|the|and|but|if|or|because|as|until|while|of|at|by|for|with|about|against|between|into|through|during|before|after|above|below|to|from|up|upon|down|in|out|on|off|over|under|again|further|then|once|here|there|when|where|why|how|all|any|both|each|few|more|most|other|some|such|no|nor|not|only|own|same|so|than|too|very|say|says|said|shall"""

  def allWords = Action {
    val headlineWords = allStories map { stories =>
      val allTheWords = stories.flatMap {s => s.headline.split(" ")}
      val interestingWords = allTheWords.filterNot(w => stopwords.contains(w.toLowerCase))

      val wordsWithCount: Map[String, Int] = interestingWords.foldLeft(Map[String, Int]()){ (m: Map[String, Int], s: String) =>
        m + (s -> (m.getOrElse(s, 0) + 1))
      }

      wordsWithCount.keys.toList.sortBy(s => wordsWithCount.get(s)).reverse
    }
    Async {
      headlineWords map {w => Ok(toJson(w))}
    }
  }

  def wordCloud = Action {
    Ok(views.html.cloud())
  }

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
    sortedStories map (x => Ok(to_timeline_json(x)))
  }}

}