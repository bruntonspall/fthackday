package model

import org.joda.time.{DateTimeZone, DateTime}
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.concurrent.Promise

trait StoryImporter {
  lazy val dateFormat = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC)

  def storiesSince(dt: DateTime) : Promise[Seq[AbstractStory]]

  def parseDate(dateString: String) = DateTime.parse(dateString, dateFormat)
}
