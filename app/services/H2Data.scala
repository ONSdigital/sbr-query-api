package services

import java.io.File
import java.sql.{ Connection, DriverManager }
import java.time.YearMonth
import javax.inject.Inject

import scala.util.{ Failure, Success, Try }

import com.typesafe.config.Config
import play.api.Logger

import models.Enterprise

/**
 * Created by ChiuA on 25/08/2017.
 */
class H2Data @Inject() (val config: Config) extends DataAccess {

  def getRecordsByQuery(period: YearMonth, query: String, filter: String): List[Enterprise] = {
    getDbConnection match {
      case Failure(thrown) =>
        Logger.error(s"${thrown.getMessage}")
        throw new Exception(s"${thrown.getMessage}")
      case Success(conn) =>
        createTable(conn)
        val current = period.toString match {
          case "2017-06" => "June"
          case "2017-08" => "August"
          case _ => "June"
        }
        val searchQuery = s"select $filter from $current where $query"
        val selectPreparedStatement = conn.prepareStatement(searchQuery)
        val rs = selectPreparedStatement.executeQuery
        val listOfEnterprises = Enterprise.rsToList(rs, filter)
        conn.close()
        listOfEnterprises
    }
  }

  def getDbConnection: Try[Connection] = {
    val DB_DRIVER: String = "org.h2.Driver"
    val DB_CONNECTION: String = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    val DB_USER: String = ""
    val DB_PASSWORD: String = ""

    Class.forName(DB_DRIVER)
    Try(DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD)).recoverWith {
      case _: Exception => Failure(new Exception("Unable to create JDBC connection to H2"))
    }
  }

  def createTable(conn: Connection) {
    val dataFirstPeriod = new File("conf/sample/201706/sbr-2500-ent-data.csv").toURI.toURL.toExternalForm
    val dataSecondPeriod = new File("conf/sample/201708/sbr-2500-ent-data.csv").toURI.toURL.toExternalForm
    val createFirstPeriod = "Create Table IF NOT EXISTS June (" +
      "entref varchar(255)," +
      "ent_name varchar(255)," +
      "ent_tradingstyle varchar(255)," +
      "ent_address1 varchar(255)," +
      "ent_address2 varchar(255)," +
      "ent_address3 varchar(255)," +
      "ent_address4 varchar(255)," +
      "ent_address5 varchar(255)," +
      "ent_postcode varchar(255)," +
      "legalstatus varchar(255)," +
      "PAYE_jobs varchar(255)," +
      "employees varchar(255)," +
      "standard_vat_turnover varchar(255)," +
      "Num_Unique_PayeRefs varchar(255)," +
      "Num_Unique_VatRefs varchar(255)," +
      "contained_rep_vat_turnover varchar(255))" +
      "as select * from CSVREAD('" + dataFirstPeriod + "',null,'fieldSeparator=,')"

    val createSecondPeriod = "Create Table IF NOT EXISTS August (" +
      "entref varchar(255)," +
      "ent_name varchar(255)," +
      "ent_tradingstyle varchar(255)," +
      "ent_address1 varchar(255)," +
      "ent_address2 varchar(255)," +
      "ent_address3 varchar(255)," +
      "ent_address4 varchar(255)," +
      "ent_address5 varchar(255)," +
      "ent_postcode varchar(255)," +
      "legalstatus varchar(255)," +
      "PAYE_jobs varchar(255)," +
      "employees varchar(255)," +
      "standard_vat_turnover varchar(255)," +
      "Num_Unique_PayeRefs varchar(255)," +
      "Num_Unique_VatRefs varchar(255)," +
      "contained_rep_vat_turnover varchar(255))" +
      "as select * from CSVREAD('" + dataSecondPeriod + "',null,'fieldSeparator=,')"
    val createPreparedStatement1 = conn.prepareStatement(createFirstPeriod)
    val createPreparedStatement2 = conn.prepareStatement(createSecondPeriod)
    createPreparedStatement1.executeUpdate
    createPreparedStatement2.executeUpdate
    createPreparedStatement1.close()
    createPreparedStatement2.close()
  }
}
