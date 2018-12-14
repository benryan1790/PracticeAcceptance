package stepdefs

import com.google.gson.JsonParser
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import pages.BasePage
import utils.Env
import utils.Constants._
import junit.framework.Assert
import scala.io.Source
import scala.util.Random

class StepDefs extends ScalaDsl with EN with Env with Matchers with BasePage {

  Given("""^I log in to request access to (.*)$""") { (enrolment: String) =>
    navigateTo(authWizardUri)
    findByName("authorityId").clear()
    findByName("authorityId").sendKeys("ben-ryan-org")
    findByName("redirectionUrl").clear()
    findByName("redirectionUrl").sendKeys(s"$enrolmentMgmtFeUri/$enrolment/request-access-tax-scheme?continue=%2FTest")
    selectFromDropdownByText("affinityGroup", "Organisation")
    clickByCSSSelector("input.button[value='Submit']")
    enrolment match {
      case "IR-SA" => assert(waitAndFindByCSSSelector("h1").getText == "Request access to Self-Assessment")
      case _ => fail(s"$enrolment is unknown, choose a valid service to enrol")
    }
  }

  When("""^the (.*) is input as (.*)$""") { (knownFact: String, kfValue: String) =>
    knownFact match {
      case "UTR" => sendKeysById("identifiers[0]", kfValue)
      case "NINO" => sendKeysById("verifiers[0]", kfValue)
      case _ => fail(s"$knownFact is unknown, enter a valid known fact")
    }
  }

  When("""^I click the (.*) button$""") { (buttonText: String) =>
    buttonText match {
      case "request access" | "continue" | "remove access"
      => clickById("submit-continue")
      case _ => fail(s"$buttonText is unknown and cannot be clicked")
    }
  }

  Then("""^the I am presented with the (.*) access requested screen$""") { (pageHeading: String) =>
    pageHeading match {
      case "IR-SA" => assert(waitAndFindByCSSSelector("h1").getText == "You've requested access to Self-Assessment")
      case _ => fail("The page's heading was not as expected")
    }
  }

  Given("""^I choose to de-enrol from (.*)""") {(enrolment: String) =>
    navigateTo(s"$enrolmentMgmtFeUri/$enrolment/remove-access-tax-scheme?continue=%2FTest")
  }

  Then("""^I am is presented with the (.*) access removed screen$""") { (pageHeading: String) =>
    pageHeading match {
      case "IR-SA" => assert(waitAndFindByCSSSelector("h1").getText == "You've removed access to Self-Assessment")
      case _ => fail("The page's heading was not as expected")
    }
  }

  Given("""^I have an activated enrolment for (.*) and request a removal$""") { (enrolment: String) =>
    navigateTo(authWizardUri)
    findByName("authorityId").clear()
    findByName("authorityId").sendKeys("ben-ryan-org")
    findByName("redirectionUrl").clear()
    findByName("redirectionUrl").sendKeys(s"$enrolmentMgmtFeUri/$enrolment/remove-access-tax-scheme?continue=%2FTest")
    selectFromDropdownByText("affinityGroup", "Organisation")
    findByName("email").sendKeys("default@example.com")
    findByName("enrolment[0].name").sendKeys("IR-SA")
    sendKeysById("input-0-0-name", "UTR")
    sendKeysById("input-0-0-value", "1231231231")
    clickByCSSSelector("input.button[value='Submit']")

  }

  Then("""^I redirect to the remove (.*) access page$""") { (service: String) =>
    assert(waitAndFindByCSSSelector("h1").getText== "Remove your organisation's access to a tax or scheme")
    assert(waitAndFindById("service-name").getText == service)
  }

  Then("""^I redirect to the Are you sure you want to remove (.*) access\? page$""") { (service: String) =>
    assert(waitAndFindByCSSSelector("h1").getText== "Are you sure you want to remove your organisation's access?")
    assert(waitAndFindById("service-name").getText == service)
  }

  When("""^I call the enrolment store stub and inject details$""") {
    groupId = "90CCF333-65D2-4BF2-A008" + Random.alphanumeric.take(12).mkString.toUpperCase
    credId = Random.nextLong().toString.take(12)
    val url = enrolmentStoreStubUrl
    val body = s"""{
    "groupId": "$groupId",
    "affinityGroup": "Organisation",
    "users": [
        {
            "credId": "$credId",
            "name": "Default User",
            "email": "default@example.com",
            "credentialRole": "Admin",
            "description": "User Description"
        }
    ],
    "enrolments": [
    	        {
            "serviceName": "IR-SA",
            "identifiers": [
                {
                    "key": "UTR",
                    "value": "11231231231"
                }
            ],
            "enrolmentFriendlyName": "IR SA Enrolment",
            "assignedUserCreds": [
                "00000123450"
            ],
            "state": "Activated",
            "enrolmentType": "principal",
            "assignedToAll": false
        }
    ]
}"""
    response = postHttpWithTimeout(url, body,10000,60000).header("content-type", "application/json").asString
  }

  Then("""^the response code is (.*)$""") {(code: Int) =>
    response.code shouldBe(code)
  }

  Then("""^I shut the browser$""") {
    quitBrowser()
  }

}
