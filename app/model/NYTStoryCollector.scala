package model

import play.api.libs.ws.WS
import play.api.libs.json.{Reads, JsArray}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{Interval, DateTime}


object NYTNewsStreamStoryCollector extends StoryImporter {

  val key = "9a82cf24873041c9afa57ae3d264cc00:1:66152292"

  implicit val reads = Reads

  def storiesSince(dt: DateTime) = {

    val now = new DateTime()

    val hourDiff = new Interval(dt, now).toDuration.getStandardHours

    WS.url("http://api.nytimes.com/svc/news/v3/content/all/all/%s?api-key=%s".format(hourDiff, key)).get.map {
      response =>
        val results = (response.json \ "results").asInstanceOf[JsArray]
        results.value map { result =>

          val title = (result \ "title").as[String]
          val tags = (result \ "des_facet") match {
            case ts: JsArray => ts.value.map{v => v.as[String] }
            case _ => Nil
          }
          val pubTime = parseDate((result \ "updated_date").as[String])

          AbstractStory(
            title,
            tags,
            pubTime,
            "NYT"
          )
        }
    }
  }
}

object NYTSearchStoryCollector extends StoryImporter {

  val key = "e9cc8b063f5a3b39a4e92319e1da45c2:4:66152292"

  implicit val reads = Reads

  def storiesSince(dt: DateTime) = { // actually stories on (and only one page at that...)

    dt.toString(ISODateTimeFormat.basicDate())

    WS.url("http://api.nytimes.com/svc/search/v1/article?format=json&query=+date:%s&fields=date,title,des_facet&api-key=%s".format(dt.toString(ISODateTimeFormat.basicDate()), key)).get.map {
      response =>
        val results = (response.json \ "results").asInstanceOf[JsArray]
        results.value map { result =>

          val title = (result \ "title").as[String]
          val tags = (result \ "des_facet") match {
            case ts: JsArray => ts.value.map{v => v.as[String] }
            case _ => Nil
          }
          val pubTime = DateTime.parse((result \ "date").as[String], ISODateTimeFormat.basicDate())

          AbstractStory(
            title,
            tags,
            pubTime,
            "FT"
          )
        }
    }
  }
}
