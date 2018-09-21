package utils

import java.time.YearMonth
import java.time.format.DateTimeFormatter

import scala.concurrent.Future

import play.api.libs.json._
import play.api.mvc.Result

/**
 * Created by chiua on 28/08/2017.
 */
object Utilities {

  def errAsJson(status: Int, code: String, msg: String): JsObject = {
    Json.obj(
      "status" -> status,
      "code" -> code,
      "message_en" -> msg
    )
  }

  implicit class ResultAugmenter(val res: Result) {
    def future: Future[Result] = {
      Future.successful(res)
    }
  }

  def periodToYearMonth(period: String): YearMonth = {
    YearMonth.parse(period.slice(0, 6), DateTimeFormatter.ofPattern("yyyyMM"))
  }
}