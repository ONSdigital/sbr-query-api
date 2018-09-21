package models
import java.io.{ BufferedWriter, File, FileWriter }
import java.sql.ResultSet
import java.util.StringJoiner

import play.api.Logger
import play.api.libs.json.{ JsValue, Json, OFormat }

import utils.RsIterator
/**
 * Created by ChiuA on 30/08/2017.
 */

case class Enterprise(
  entref: Option[String] = None,
  ent_name: Option[String] = None,
  ent_tradingstyle: Option[String] = None,
  ent_address1: Option[String] = None,
  ent_address2: Option[String] = None,
  ent_address3: Option[String] = None,
  ent_address4: Option[String] = None,
  ent_address5: Option[String] = None,
  ent_postcode: Option[String] = None,
  legalstatus: Option[String] = None,
  PAYE_jobs: Option[String] = None,
  employees: Option[String] = None,
  standard_vat_turnover: Option[String] = None,
  Num_Unique_PayeRefs: Option[String] = None,
  Num_Unique_VatRefs: Option[String] = None,
  contained_rep_vat_turnover: Option[String] = None
)

object Enterprise {
  implicit val unitFormat: OFormat[Enterprise] = Json.format[Enterprise]

  implicit def string2Option(s: String): Some[String] = Some(s)

  def listToJson(e: List[Enterprise]): JsValue = Json.toJson(e)

  def createCsv(e: List[Enterprise], filter: String): String = {
    val fileName = new File("Frame.csv").toURI.toURL.toExternalForm.replace("file:", "")
    val bw = new BufferedWriter(new FileWriter(fileName))
    bw.write(filter)
    for (a <- e) {
      val joiner = new StringJoiner(",")
      for (b <- a.productIterator.toList) {
        Logger.info("" + b)
        val value = b.toString.replace("Some(null", "").replace("None", "").replace("Some(", "")
        if (value != "") {
          val record = value.substring(0, value.length - 1)
          joiner.add(record)
        }
      }
      bw.write("\n")
      bw.write(joiner.toString())
    }
    bw.close()
    fileName
  }

  def checkFilter(x: ResultSet, filter: String, header: String): Option[String] = {
    var test: Option[String] = None
    if (filter.contains(header)) {
      try
        test = x.getString(header)
      catch {
        case e: ArrayIndexOutOfBoundsException =>
          test = None
      }
    }
    test
  }

  def rsToList(rs: ResultSet, filter: String): List[Enterprise] = {
    new RsIterator(rs).map(x => {
      Enterprise(
        checkFilter(x, filter, "entref"),
        checkFilter(x, filter, "ent_name"),
        checkFilter(x, filter, "ent_tradingstyle"),
        checkFilter(x, filter, "ent_address1"),
        checkFilter(x, filter, "ent_address2"),
        checkFilter(x, filter, "ent_address3"),
        checkFilter(x, filter, "ent_address4"),
        checkFilter(x, filter, "ent_address5"),
        checkFilter(x, filter, "ent_postcode"),
        checkFilter(x, filter, "legalstatus"),
        checkFilter(x, filter, "PAYE_jobs"),
        checkFilter(x, filter, "employees"),
        checkFilter(x, filter, "standard_vat_turnover"),
        checkFilter(x, filter, "Num_Unique_PayeRefs"),
        checkFilter(x, filter, "Num_Unique_VatRefs"),
        checkFilter(x, filter, "contained_rep_vat_turnover")
      )
    }).toList
  }
}