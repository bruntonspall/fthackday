package model

import play.api.libs.ws.WS
import play.api.libs.json.{Reads, JsArray}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{Interval, DateTime}


object NYTStoryCollector {

  val key = "9a82cf24873041c9afa57ae3d264cc00:1:66152292"

  implicit val reads = Reads

  def storiesSince(dt: DateTime) = {

    val now = new DateTime()

    val hourDiff = new Interval(dt, now).toDuration.getStandardHours

    WS.url("http://api.nytimes.com/svc/news/v3/content/all/all/%s?api-key=%s".format(hourDiff, key)).get.map {
      response =>
        val results = (response.json \ "results").asInstanceOf[JsArray]
        results.value map { result =>

          println("title = " + (result \ "title").as[String])
          println("updated date = " + (result \ "updated_date").as[String])
          println("tags = ")
          (result \ "des_facet") match {
            case ts: JsArray => ts.value.foreach{v => println("\t"+ v.as[String])}
            case _ => println("NO TAGS")
          }

          val title = (result \ "title").as[String]
          val tags = (result \ "des_facet") match {
            case ts: JsArray => ts.value.map{v => v.as[String] }
            case _ => Nil
          }
          val pubTime = DateTime.parse((result \ "updated_date").as[String], ISODateTimeFormat.dateTimeNoMillis())

          AbstractStory(
            title,
            tags,
            pubTime
          )
        }
    }
  }
}
