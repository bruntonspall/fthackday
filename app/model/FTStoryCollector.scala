package model

import org.joda.time.DateTime
import play.api.libs.ws.{Response, WS}
import model.AbstractStory
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{JsValue, Reads, JsArray}

object FTStoryCollector {
  def storiesSince(dt: DateTime) = {
    implicit val reads = Reads

    WS.url("http://api.ft.com/content/search/v1?apiKey=8f4a8b83f48c4eecf9b6b64c90a4451b").post(
      """{ "queryString": "-zxxzxz AND (lastPublishDateTime:>2012-05-23T14:00:00Z)"}"""
    ) map {
      response =>
        (response.json \ "results" \\ "results").flatMap(_.asInstanceOf[JsArray].value map {
          result : JsValue =>
            AbstractStory(
              (result \ "title" \ "title").as[String],
              Nil,
              DateTime.parse((result \ "lifecycle" \ "lastPublishDateTime").as[String], ISODateTimeFormat.dateTimeNoMillis())
            )
        })
    }
  }
}
