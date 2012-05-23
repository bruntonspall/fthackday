package model
import play.api.libs.ws.WS
import org.joda.time.DateTime
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat}
import play.api.libs.json.{Writes, JsArray}

object GuImporter extends StoryImporter {
  def storiesSince(dt: DateTime) =
    WS.url("http://content.guardianapis.com/search.json?show-tags=keyword&from-date=%s".format(dateFormat.print(dt)))
      .get.map { response =>
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
