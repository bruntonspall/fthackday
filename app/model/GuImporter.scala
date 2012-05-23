package model
import play.api.libs.ws.WS
import play.api.libs.json.JsArray
import org.joda.time.DateTime
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat}

object GuImporter {
  def storiesSince(dt: DateTime) = WS.url("http://content.guardianapis.com/search.json?show-tags=keyword").get().map {
    response =>
    (response.json \ "response" \ "results").asInstanceOf[JsArray].value.map {
      result =>
        AbstractStory(
          (result \ "webTitle").as[String],
          (result \ "tags" \\ "webTitle").map(_.as[String]),
          DateTime.parse((result \ "webPublicationDate").as[String], ISODateTimeFormat.dateTimeNoMillis))
    }
  }
}
