package services

import java.time.YearMonth

import models.Enterprise

/**
 * Created by ChiuA on 29/08/2017.
 */
trait DataAccess {
  def getRecordsByQuery(period: YearMonth, query: String, filter: String): List[Enterprise]
}
