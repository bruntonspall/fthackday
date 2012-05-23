package model

import org.joda.time.DateTime
import play.api.libs.ws.WS
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{JsValue, Reads, JsArray}

object FTStoryCollector extends StoryImporter {
  def storiesSince(dt: DateTime) = {
    implicit val reads = Reads

    WS.url("http://api.ft.com/content/search/v1?apiKey=8f4a8b83f48c4eecf9b6b64c90a4451b").post(
      """{ "queryString": "-zxxzxz AND (lastPublishDateTime:>%s)"}""".format(dateFormat.print(dt))
    ) map {
      response =>
        (response.json \ "results" \\ "results").flatMap(_.asInstanceOf[JsArray].value map {
          result : JsValue =>
            AbstractStory(
              (result \ "title" \ "title").as[String],
              Nil,
              parseDate((result \ "lifecycle" \ "lastPublishDateTime").as[String])
            )
        })
    }
  }
}
