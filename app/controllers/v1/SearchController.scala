package controllers.v1

import java.time.DateTimeException
import javax.inject.Inject

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

import com.typesafe.config.Config
import play.api.Logger
import play.api.mvc.{ Action, AnyContent, Controller, Result }

import utils.Utilities._
import models._
import services._

/**
 * Created by ChiuA on 29/08/2017.
 */
class SearchController @Inject() (data: DataAccess, val config: Config) extends Controller {

  def getFrame(period: String, query: String, filter: String): Action[AnyContent] = {
    Action.async { implicit request =>
      Logger.info(s"Searching for $query, in period: $period showing columns: $filter")
      Try(periodToYearMonth(period)) match {
        case Success(validPeriod) => Try(data.getRecordsByQuery(validPeriod, query, filter)) match {
          case Success(results) => resultsMatcher(results, query)
          case Failure(e) => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "Internal Server Error", s"An error has occurred, please contact the server administrator $e")).future
        }
        case Failure(_: DateTimeException) => UnprocessableEntity(errAsJson(UNPROCESSABLE_ENTITY, "Unprocessable Entity", "Please ensure the period is in the following format: YYYYMM")).future
        case _ => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "Internal Server Error", s"An error has occurred, please contact the server administrator")).future
      }
    }
  }

  def getCsv(period: String, query: String, filter: String): Action[AnyContent] = {
    Action.async { implicit request =>
      Logger.info(s"Creating CSV for $query, in period: $period showing columns: $filter")
      Try(periodToYearMonth(period)) match {
        case Success(validPeriod) => Try(data.getRecordsByQuery(validPeriod, query, filter)) match {
          case Success(results) => getDownload(results, query, filter)
          case Failure(e) => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "Internal Server Error", s"An error has occurred, please contact the server administrator $e")).future
        }
        case Failure(_: DateTimeException) => UnprocessableEntity(errAsJson(UNPROCESSABLE_ENTITY, "Unprocessable Entity", "Please ensure the period is in the following format: YYYYMM")).future
        case _ => InternalServerError(errAsJson(INTERNAL_SERVER_ERROR, "Internal Server Error", s"An error has occurred, please contact the server administrator")).future
      }
    }
  }

  def resultsMatcher(results: List[Enterprise], query: String): Future[Result] = results match {
    case Nil => NotFound(errAsJson(404, "Not Found", s"Could not find query:  $query")).future
    case x => Ok(Enterprise.listToJson(x)).future
  }

  def getDownload(results: List[Enterprise], query: String, filter: String): Future[Result] = results match {
    case Nil => NotFound(errAsJson(404, "Not Found", s"Could not find query: $query")).future
    case x => Ok.sendFile(new java.io.File(Enterprise.createCsv(x, filter))).future
  }
}