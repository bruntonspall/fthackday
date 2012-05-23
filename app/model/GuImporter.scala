package model
import play.api.libs.ws.WS
import org.joda.time.DateTime
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat}
import play.api.libs.json.{Writes, JsArray}
import play.api.Logger

object GuImporter extends StoryImporter {
  val log = Logger("GuImporter")
  def storiesSince(dt: DateTime) = {
    val url = "http://content.guardianapis.com/search.json?show-tags=keyword&from-date="+
      ISODateTimeFormat.dateTimeNoMillis.withZoneUTC().print(dt)
    log.error("Fetching from "+url)
    WS.url(url)
      .get.map {
        response =>
        (response.json \ "response" \ "results").asInstanceOf[JsArray].value.map {
          result =>
            AbstractStory(
              (result \ "webTitle").as[String],
              (result \ "tags" \\ "webTitle").map(_.as[String]),
              parseDate((result \ "webPublicationDate").as[String])
            )
        }
    }
  }
}
