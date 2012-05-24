package controllers

import play.api._
import libs.concurrent.Promise
import libs.json._
import play.api.mvc._
import play.api.libs.json.Json._
import model._
import org.joda.time.format.ISODateTimeFormat
import util.Random
import org.joda.time.{Interval, DateTime}

object Application extends Controller {
  implicit val reads = Reads
  import AbstractStory._

  def colour_for(source: String):String = source match {
    case "FT" => "salmon"
    case "GU" => "#005689"
    case "NYT" => "gray"
    case "WP" => "#F1831E"
    case "MO" => "#DD22E0"
    case _ => "black"
  }

  def icon_for(source: String) = routes.Assets.at("images/new-"+source+".ico").toString

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  def timeline = Action {
    Ok(views.html.timeline())
  }

  def headline = Action {
    Ok(views.html.headline())
  }

  def allStories = {
    val dt = DateTime.now.minusHours(24)
    val allStories : Promise[Seq[AbstractStory]] = for {
      gustory <- GUStoryCollector.storiesSince(dt)
      ftstory <- FTStoryCollector.storiesSince(dt)
      nytstory <- NYTSearchStoryCollector.storiesSince(dt)
      wpstory <- WPStoryCollector.storiesSince(dt)
      mostory <- MOStoryCollector.storiesSince(dt)
    } yield gustory ++ ftstory ++ nytstory ++ wpstory ++ mostory

    allStories
  }

  def sortedStories = allStories map { stories => stories.sortWith((a,b) => a.publicationDate.isBefore(b.publicationDate))}

  val random = new Random()

  def top4PerProvider = allStories map { stories =>
    val storiesBySource = stories.groupBy(_.source)
    val topStoriesBySource = storiesBySource.mapValues(ss => ss.sortWith((a,b) => a.publicationDate.isBefore(b.publicationDate)).take(4))

    val topStories = topStoriesBySource.values.flatMap(ss=>ss).toList
    random.shuffle(topStories)
  }

  def topGrid = Action { Async { top4PerProvider map (foo => Ok(views.html.topgrid(foo))) }}

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

//  def stats_mo = Action { Ok("") }
  def stats_mo = Action { Async {
    MOStoryCollector.storiesSince(DateTime.now.minusHours(24)) map (x => Ok(toJson(x)))
  }}


  val stopwords = """-|i|me|my|myself|we|us|our|ours|ourselves|you|your|yours|yourself|yourselves|he|him|his|himself|she|her|hers|herself|it|its|itself|they|them|their|theirs|themselves|what|which|who|whom|whose|this|that|these|those|am|is|are|was|were|be|been|being|have|has|had|having|do|does|did|doing|will|would|should|can|could|ought|i'm|you're|he's|she's|it's|we're|they're|i've|you've|we've|they've|i'd|you'd|he'd|she'd|we'd|they'd|i'll|you'll|he'll|she'll|we'll|they'll|isn't|aren't|wasn't|weren't|hasn't|haven't|hadn't|doesn't|don't|didn't|won't|wouldn't|shan't|shouldn't|can't|cannot|couldn't|mustn't|let's|that's|who's|what's|here's|there's|when's|where's|why's|how's|a|an|the|and|but|if|or|because|as|until|while|of|at|by|for|with|about|against|between|into|through|during|before|after|above|below|to|from|up|upon|down|in|out|on|off|over|under|again|further|then|once|here|there|when|where|why|how|all|any|both|each|few|more|most|other|some|such|no|nor|not|only|own|same|so|than|too|very|say|says|said|shall"""

  def allWords = Action {
    val headlineWords = allStories map { stories =>
      val allTheWords = stories.flatMap {s => s.headline.split(" ")}
      val interestingWords = allTheWords.filterNot(w => stopwords.contains(w.toLowerCase))

      val wordsWithCount: Map[String, Int] = interestingWords.foldLeft(Map[String, Int]()){ (m: Map[String, Int], s: String) =>
        m + (s -> (m.getOrElse(s, 0) + 1))
      }

      wordsWithCount.keys.toList.sortBy(s => wordsWithCount.get(s)).reverse.take(250)
    }
    Async {
      headlineWords map {w => Ok(toJson(w))}
    }
  }

  def wordCloud = Action {
    Ok(views.html.cloud())
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
          :: ("icon") -> JsString(icon_for(o.source))
          :: ("description") -> JsString("<p>%s</p><p><b>tags:</b>%s</p>".format(
          o.trail,o.tags.mkString(", ")))
          :: Nil)
        }
      ) :: Nil
    )
  }

  def stories_timeline_js = Action { Async {
    sortedStories map (x => Ok(to_timeline_json(x)))
  }}

  def metric(source: String, start: String, stop: String, step: Int) = Action {
    val startTime = DateTime.parse(start, ISODateTimeFormat.dateTime())
    val endTime = DateTime.parse(stop, ISODateTimeFormat.dateTime())
    val steps = new Interval(startTime, endTime).toDurationMillis / step

    Async {
      collector(source).storiesSince(DateTime.now.minusHours(24)) map (all => {
        val counts: Seq[Int] = for {
          i <- 1L to steps
        } yield {
          val stepStart = startTime.plus(step * (i - 1))
          val stepEnd = startTime.plus(step * (i))
          val inPeriod = all.filter {x => x.publicationDate.isAfter(stepStart) && x.publicationDate.isBefore(stepEnd)}

          inPeriod.size
        }
        implicit val writes = Writes
        Ok(toJson(counts))
      })
    }
  }
  val collector = Map(
    "Guardian" -> GUStoryCollector,
    "FT" -> FTStoryCollector,
    "Daily Fail" -> MOStoryCollector,
    "NYT" -> NYTSearchStoryCollector,
    "Wordpress" -> WPStoryCollector
  )
}
