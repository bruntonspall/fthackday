package model

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json._
import play.api.libs.json.Json._

case class AbstractStory(headline: String, trail: String, tags:Seq[String], publicationDate: DateTime, source: String)

object AbstractStory {
  implicit object AbstractStoryJsonWriter extends Writes[AbstractStory] {
    lazy val timeFormatter = ISODateTimeFormat.dateTimeNoMillis.withZoneUTC()
    def writes(o: AbstractStory) = {
      JsObject(
        ("headline" -> JsString(o.headline))
          :: ("trail" -> JsString(o.trail))
          :: ("publicationDate") -> JsString(timeFormatter.print(o.publicationDate))
          :: ("tags") -> JsArray(o.tags.map(JsString(_)))
          :: ("source") -> JsString(o.source)
          :: Nil)
    }
  }
  implicit object SeqAbstractStoryJsonWriter extends Writes[Seq[AbstractStory]] {
    lazy val timeFormatter = ISODateTimeFormat.dateTimeNoMillis.withZoneUTC()
    def writes(s: Seq[AbstractStory]) = {
      JsArray(s.map(toJson(_)))
    }
  }
}
