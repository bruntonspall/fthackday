package model

import play.api.libs.json.JsArray
import play.api.Logger
import play.api.libs.ws.{Response, WS}
import play.api.libs.concurrent.Promise
import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter, ISODateTimeFormat}

object MOStoryCollector extends StoryImporter {
  val log = Logger("MOStoryCollector")
  // Thu, 24 May 2012 12:03:36 GMT
  override lazy val dateFormat = DateTimeFormat.forPattern("E, dd MMM yyyy HH:mm:ss")

  def rss_to_story(response: Response) = (response.xml \\ "item").map {
    item =>
      AbstractStory(
        (item \ "title").text,
        (item \ "description").text,
        Nil,
        parseDate((item \ "pubDate").text.reverse.drop(4).reverse),
        "MO"
      )
  }

  def storiesSince(dt: DateTime):Promise[Seq[AbstractStory]] = {
    val url = "http://www.dailymail.co.uk/home/index.rss"
    log.error("Fetching from " + url)
    WS.url(url).get.map {
        rss_to_story(_)
      }
  }
}
