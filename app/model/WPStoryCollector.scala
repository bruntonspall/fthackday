package model

import play.api.libs.ws.WS
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.JsArray
import play.api.Logger

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
              "",
              Nil,
              parseDate((result \ "date").as[String]),
              "WP"
            )
        }
      }
    }
  }
}
