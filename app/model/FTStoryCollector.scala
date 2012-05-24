package model

import org.joda.time.DateTime
import play.api.libs.ws.WS
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.{JsValue, Reads, JsArray}
import play.api.libs.concurrent.Promise
import play.api.cache.Cache
import cache.CacheThatMakesPromises
import play.api.{Logger, Application}

object FTStoryCollector extends StoryImporter {
  import play.api.Play.current

  val log = Logger(this.getClass.getSimpleName)

  def storiesSince(dt: DateTime) = {
    implicit val reads = Reads

    WS.url("http://api.ft.com/content/search/v1?apiKey=8f4a8b83f48c4eecf9b6b64c90a4451b").post(
      """{ "queryString": "lastPublishDateTime:>%s"}""".format(dateFormat.print(dt))
    ) flatMap { response =>
      Promise.sequence((response.json \ "results" \\ "results").flatMap(_.asInstanceOf[JsArray].value map {
        result : JsValue =>
          val contentApiUrl = (result \ "apiUrl").as[String]

          CacheThatMakesPromises.getOrElse(contentApiUrl) {
            WS.url("%s?apiKey=8f4a8b83f48c4eecf9b6b64c90a4451b".format(contentApiUrl)).get() map { itemResponse =>
              itemResponse.status match {
                case 200 => {
                  Right(AbstractStory(
                    (result \ "title" \ "title").as[String],
                    (itemResponse.json \ "item" \ "metadata" \ "tags" \\ "name").map(_.as[String]),
                    parseDate((result \ "lifecycle" \ "lastPublishDateTime").as[String]),
                    "FT"
                  ))
                }
                case code => Left("Status code:%s was unexpected, \nresponse:\n%s" format (code, itemResponse.body))
              }
            }
          }
      })) map {x :Seq[Either[String, AbstractStory]] => (x map (_.fold ({l => log.error(l); None} , {r => Some(r)}))).flatten }
    }
  }
}
