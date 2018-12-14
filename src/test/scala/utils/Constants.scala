package utils

import scalaj.http.HttpResponse

object Constants extends Env {

//  *********************URIS********************
  val enrolmentStoreStubUrl = "http://localhost:9595/enrolment-store-stub/data"

//  *********************Other Variables****************
  var response: HttpResponse[String] = _
  var groupId = ""
  var credId = ""

}
