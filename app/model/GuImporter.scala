package model
import play.api.libs.ws.WS
import play.api.libs.json.JsArray
import org.joda.time.DateTime
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormat}

class GuImporter {
  val json = WS.url("http://content.guardianapis.com/search.json").get().map{ response =>
    (response.json \ "response" \ "results").asInstanceOf[JsArray].value.map { result =>
      AbstractStory(result \ "webTitle" toString(), Nil, DateTime.parse(result \ "webPublicationDate" toString(), ISODateTimeFormat.dateTimeNoMillis))
    }
  }
}
