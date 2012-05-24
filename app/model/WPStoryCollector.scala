package model

import play.api.libs.ws.WS
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.Logger
import play.api.libs.json.{JsObject, JsArray}

object WPStoryCollector extends StoryImporter {
  val log = Logger("WPStoryCollector")

  def storiesSince(dt: DateTime) = {
    val url = "https://public-api.wordpress.com/rest/v1/freshly-pressed/?number=50&after="+
      ISODateTimeFormat.dateTimeNoMillis.withZoneUTC().print(dt.minusHours(24))

    log.error("Fetching from " + url)
    WS.url(url).get.map {
      response => {
        log.warn("Got response "+response)
        (response.json \ "posts").asInstanceOf[JsArray].value.map { result =>
            AbstractStory(
              (result \ "title").as[String],
              (result \ "excerpt").as[String],
              (result \ "tags") match {
                case a:JsObject => a.keys.toSeq
                case _ => Nil
              }
              ,
              parseDate((result \ "date").as[String]),
              "WP"
            )
        }
      }
    }
  }
}
