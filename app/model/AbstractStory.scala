package model

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{JsArray, JsString, JsObject, Writes}

case class AbstractStory(headline: String, tags:Seq[String], publicationDate: DateTime)

object AbstractStory {
  implicit object GuImporterJsonWriter extends Writes[AbstractStory] {
    lazy val timeFormatter = ISODateTimeFormat.dateTimeNoMillis.withZoneUTC()
    def writes(o: AbstractStory) = {
      JsObject(
        ("headline" -> JsString(o.headline))
          :: ("publicationDate") -> JsString(timeFormatter.print(o.publicationDate))
          :: ("tags") -> JsArray(o.tags.map(JsString(_)))
          :: Nil)
    }
  }
}
