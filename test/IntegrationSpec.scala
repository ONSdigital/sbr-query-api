package test

import play.api.test.Helpers._

class IntegrationSpec extends TestUtils {

  "Get by enterprise number" should {

    "return correct company" in {
      val enterpriseNumber = "9900156115"
      val res = fakeRequest(s"/v1/enterprise/period/201708/query/EntRef=${enterpriseNumber}/filter/entref")
      status(res) mustBe OK
      contentType(res) mustBe Some("application/json")
    }

    "return 404 if enterprise is not found" in {
      val enterpriseNumber = "9900156111"
      val res = fakeRequest(s"/v1/enterprise/period/201708/query/EntRef=${enterpriseNumber}/filter/entref")
      status(res) mustBe NOT_FOUND
      contentType(res) mustBe Some("application/json")
    }
  }

  "Get frame download" should {

    "download file" in {
      val enterpriseNumber = "9900156115"
      val res = fakeRequest(s"/v1/download/period/201708/query/EntRef=${enterpriseNumber}/filter/entref")
      status(res) mustBe OK
      contentType(res) mustBe Some("text/csv")
    }

    "return 404 if enterprise is not found" in {
      val enterpriseNumber = "9900156111"
      val res = fakeRequest(s"/v1/download/period/201708/query/EntRef=${enterpriseNumber}/filter/entref")
      status(res) mustBe NOT_FOUND
      contentType(res) mustBe Some("application/json")
    }

  }

}
