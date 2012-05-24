package model
import org.joda.time.DateTime
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat}
import play.api.libs.json.{Writes, JsArray}
import play.api.Logger
import play.api.libs.ws.{Response, WS}
import play.api.libs.concurrent.Promise

object GUStoryCollector extends StoryImporter {
  val log = Logger("GUStoryCollector")
  def api_to_json(response:Response) = (response.json \ "response" \ "results").asInstanceOf[JsArray].value.map { result =>
    AbstractStory(
      (result \ "webTitle").as[String],
      (result \ "fields" \ "trailText").as[String],
      (result \ "tags" \\ "webTitle").map(_.as[String]),
      parseDate((result \ "webPublicationDate").as[String]),
      "GU"
    )
  }
  def storiesSince(dt: DateTime) = {
    val url = "http://content.guardianapis.com/search.json?show-tags=keyword&page-size=50&show-fields=trailText&from-date="+
      ISODateTimeFormat.dateTimeNoMillis.withZoneUTC().print(dt)
    val url2 = url+"&page=2"
    log.error("Fetching from "+url)
    Promise.sequence(WS.url(url).get :: WS.url(url2).get :: Nil map { _.map { api_to_json(_) } }).map (_.flatten)
  }
}
