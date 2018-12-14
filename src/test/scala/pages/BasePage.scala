package pages

import junit.framework.Assert.assertEquals
import org.openqa.selenium._
import org.openqa.selenium.support.ui
import org.scalatest.Matchers
import org.apache.commons.io.FileUtils
import utils.Driver
import utils.Constants
import java.io.{File, PrintWriter}
import java.time.LocalDate
import java.util.concurrent.TimeUnit

import org.mongodb.scala.{MongoCollection, MongoDatabase}
import org.mongodb.scala.bson.collection.immutable.Document
import org.openqa.selenium.support.ui.{ExpectedConditions, Select, WebDriverWait}
import scalaj.http.{Http, HttpOptions}

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration



object BasePage extends BasePage

trait BasePage extends Matchers{

  val driver = Driver.getInstance()
  var initialWindows = driver.getWindowHandles.asScala

  def quitBrowser() = driver.quit()

  def back() = driver.navigate().back()

  def getCurrentUrl = driver.getCurrentUrl

  def getTitle = driver.getTitle

  def switchTo = driver.switchTo

  def navigateTo(url: String) = driver.navigate().to(url)

  def findByXPath(XPath: String) = driver.findElement(By.xpath(XPath))

  //Finds a certain element due to what CSSSelector you pass in from the stepdef
  def findByCSSSelector(cssSelector: String) = driver.findElement(By.cssSelector(cssSelector))

  def findElementsByXPath(XPath: String) = driver.findElements(By.xpath(XPath))

  //Clicks on a certain element due to what XPath you pass in from the stepdef
  def clickByXPath(XPath: String) = driver.findElement(By.xpath(XPath)).click()

  def clickByCSSSelector(cssSelector: String) = driver.findElement(By.cssSelector(cssSelector)).click()

  //Clicks on a certain element due to what XPath you pass in from the stepdef
  def clickById(Id: String) = driver.findElement(By.id(Id)).click()

  //Finds a certain element due to what XPath you pass in from the stepdef
  def findById(id: String) = driver.findElement(By.id(id))

  def findByName(name: String) = driver.findElement(By.name(name))

  def waitAndFindById(Id: String) = {
    val pageDisplayed = isDisplayedId(Id)
    assertEquals(s"The $Id was not displayed on the screen", true, pageDisplayed)
    driver.findElement(By.id(Id))
  }

  def waitAndFindByXpath(XPath: String): WebElement = {
    val pageDisplayed = isDisplayedXPath(XPath)
    assertEquals(s"The $XPath was not displayed on the screen", true, pageDisplayed)
    driver.findElement(By.xpath(XPath))
  }

  def waitForPostProcessOnSignIn(XPath: String, heading: String): WebElement = {
    assertEquals(s"The $XPath with heading: $heading was not displayed on the screen", true, isDisplayedSlowLoadingElement(XPath, heading))
    driver.findElement(By.xpath(XPath))
  }

  def waitForPostProcessSignInError(XPath: String, error: String): WebElement = {
    val pageDisplayed = isDisplayedSlowLoadingElement(XPath, error)
    assertEquals(s"The $XPath with heading: $error was not displayed on the screen", true, pageDisplayed)
    driver.findElement(By.xpath(XPath))
  }

  def waitAndClickByXPath(XPath: String) = {
    val pageDisplayed = isDisplayedXPath(XPath)
    assertEquals(s"The $XPath was not displayed on the screen", true, pageDisplayed)
    driver.findElement(By.xpath(XPath)).click()
  }

  def selectFromDropdownByText(menuName: String, optionName: String) = {
    val menu = findByName(menuName)
    val selectElement = new Select(menu)
    selectElement.selectByVisibleText(optionName)
  }

  def waitAndClickByCssSelector(cssSelector: String) = {
    val pageDisplayed = isDisplayedXPath(cssSelector)
    assertEquals(s"The $cssSelector was not displayed on the screen", true, pageDisplayed)
    driver.findElement(By.xpath(cssSelector)).click()
  }

  def waitAndFindByCSSSelector(CSSSelector: String) = {
    val pageDisplayed = isDisplayedCSSSelector(CSSSelector)
    assertEquals(s"The $CSSSelector was not displayed on the screen", true, pageDisplayed)
    driver.findElement(By.cssSelector(CSSSelector))
  }

  def waitAndClickById(id: String) = {
    val pageDisplayed = isDisplayedId(id)
    assertEquals(s"The $id id was not displayed on the screen", true, pageDisplayed)
    driver.findElement(By.id(id)).click()
  }

  def sendKeysById(id: String, value: String) = {
    isDisplayedId(id)
    val element = driver.findElement(By.id(id))
    driver.asInstanceOf[JavascriptExecutor].executeScript("arguments[0].value = arguments[1];", element, value)
  }

  def removeTarget(id: String) = {
    driver.asInstanceOf[JavascriptExecutor].executeScript("document.getElementById(arguments[0]).removeAttribute('target')", id)
  }

  def sendKeysByXpath(xpath: String, value: String) = {
    isDisplayedXPath(xpath)
    waitAndFindByXpath(xpath).clear()
    waitAndFindByXpath(xpath).sendKeys(value)
  }

  //Switch to confirmation pop up
  def alert() = {
    browserWait()
    driver.switchTo().alert()
  }

  private def defaultScreenShotName = System.currentTimeMillis + ".png"

  def takeScreenShot(fileName: String = defaultScreenShotName, languageDirectory: String) {
    require(fileName != "", "Filename of screenshot cannot be empty")

    val fileDirectory = LocalDate.now()
    val scrFile = driver.asInstanceOf[TakesScreenshot].getScreenshotAs(OutputType.FILE)
    try {
      val destFile = new File(s"Screenshots/$fileDirectory/$languageDirectory/$fileName" + ".png")
      FileUtils.copyFile(scrFile, destFile)
    } catch {
      case e: RuntimeException => throw new RuntimeException("error when taking the snapshot", e)
    }
  }

  def zoomIn() {
    driver.findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.CONTROL, Keys.ADD))
  }

  def pageDown() {
    driver.findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.PAGE_DOWN))
  }

  def pageUp() {
    driver.findElement(By.tagName("html")).sendKeys(Keys.chord(Keys.PAGE_UP))
  }

  def browserWait() = driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS)

  def isDisplayedSlowLoadingElement(xpath: String, elementText: String, timeOutInSeconds: Int = 10): Boolean = {
    val endTime = System.currentTimeMillis + timeOutInSeconds * 1000
    while (System.currentTimeMillis < endTime) {
      try {
        val element = findByXPath(xpath)
        if (element.isDisplayed && element.getText == elementText) {
          return true
        }
      } catch {
        case _: Throwable => false
      }
      Thread.sleep(500)
    }
    false
  }

  //  Find by XPath always returns a noSuchElementException when it returns false, so the below handles this
  def isDisplayedXPath(xpath: String, timeOutInSeconds: Int = 10): Boolean = {
    val endTime = System.currentTimeMillis + timeOutInSeconds * 1000
    while (System.currentTimeMillis < endTime) {
      try {
        if (findByXPath(xpath).isDisplayed) {
          return true
        }
      } catch {
        case _: Throwable => false
      }
      Thread.sleep(500)

    }
    false
  }


  //  Find by CSSSelector always returns a noSuchElementException when it returns false, so the below handles this
  def isDisplayedCSSSelector(cssSelector: String, timeOutInSeconds: Int = 10): Boolean = {
    val endTime = System.currentTimeMillis + timeOutInSeconds * 1000
    while (System.currentTimeMillis < endTime) {
      try {
        if (findByCSSSelector(cssSelector).isDisplayed) {
          return true
        }
      } catch {
        case _: Throwable => false
      }
      Thread.sleep(500)

    }
    false
  }

  //  Find by XPath always returns a noSuchElementException when it returns false, so the below handles this
  def isDisplayedId(id: String, timeOutInSeconds: Int = 10): Boolean = {
    val endTime = System.currentTimeMillis + timeOutInSeconds * 1000
    while (System.currentTimeMillis < endTime) {
      try {
        if (findById(id).isDisplayed) {
          return true
        }
      } catch {
        case _: Throwable => false
      }
      Thread.sleep(500)
    }
    false
  }

  def isSelectedId(id: String, timeOutInSeconds: Int = 10): Boolean = {
    val endTime = System.currentTimeMillis + timeOutInSeconds * 1000
    while (System.currentTimeMillis < endTime) {
      try {
        if (findById(id).isSelected) {
          return true
        }
      } catch {
        case _: Throwable => false
      }
      Thread.sleep(500)
    }
    false
  }

  def isAlertPresent(timeOutInSeconds: Int = 10): Boolean = {
    val endTime = System.currentTimeMillis + timeOutInSeconds * 1000
    while (System.currentTimeMillis < endTime) {
      try {
        driver.switchTo().alert(); return true
      } catch {
        case _: Throwable => false
      }
      Thread.sleep(500)
    }
    false
  }

  def waitTillElementVisible(xpath: String) = {
    val wait = new WebDriverWait(driver, 10)
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)))
  }

  def addCookie(cookie: Cookie) = driver.manage.addCookie(cookie)

  def getCookies = driver.manage.getCookies().asScala

  def getCookieByName(name: String) = driver.manage.getCookieNamed(name)

  def checkCookieByName(name: String): Option[Cookie] =
    try {
      Some(driver.manage.getCookieNamed(name))
    } catch {
      case _ => None
    }

  def deletecookiebyName(name: String) = driver.manage.deleteCookieNamed(name)

  def getTextbyTagName = driver.findElement(By.tagName("body")).getText

  def uglyPrint(pretty: String) = pretty.replaceAll("\\{  ", "\\{")
    .replaceAll("\n", "")
    .replaceAll("  ", "")
    .replaceAll(" : ", ":")
    .replaceAll(", ", ",")
    .replaceAll(" \\[", "\\[")
    .replaceAll("\\[ ", "\\[")
    .replaceAll(" \\]", "\\]")
    .replaceAll("\\] ", "\\]")

  def isEmptyById(id: String): Boolean = {
    assertEquals(s"The $id id was not displayed on the screen", true, isDisplayedId(id))
    driver.findElement(By.id(id)).getText.isEmpty
  }

  def copyByID(id: String) = {
    waitAndFindById(id).sendKeys(Keys.CONTROL, "a")
    waitAndFindById(id).sendKeys(Keys.CONTROL, "c")
  }

  def pasteByID(id: String) = {
    waitAndFindById(id).clear()
    waitAndFindById(id).sendKeys(Keys.CONTROL, "v")
  }

  def clickByLinkText(linkText: String) = driver.findElement(By.linkText(linkText)).click()

  def getCurrentWindows {
    initialWindows = driver.getWindowHandles.asScala
  }

  def getNewTab {
    val postTabWindows = driver.getWindowHandles.asScala
    val latestWindowSet = postTabWindows.diff(initialWindows)
    driver.switchTo().window(latestWindowSet.head)
  }

  def closeNewTab {
    driver.close()
    driver.switchTo().window(initialWindows.head)
  }

  def getHttpWithTimeout(url: String, connectionTimeout: Int = 10000, readTimeout: Int = 60000) = {
    Http(url).option(HttpOptions.connTimeout(connectionTimeout)).option(HttpOptions.readTimeout(readTimeout))
  }

  def postHttpWithTimeout(url: String, body: String, connectionTimeout: Int = 10000, readTimeout: Int = 60000) = {
    Http(url).header("content-type", "application/json").postData(body).option(HttpOptions.connTimeout(connectionTimeout)).option(HttpOptions.readTimeout(readTimeout))
  }

  def putHttpWithTimeout(url: String, body: String, connectionTimeout: Int = 10000, readTimeout: Int = 60000) = {
    Http(url).put(body).option(HttpOptions.connTimeout(connectionTimeout)).option(HttpOptions.readTimeout(readTimeout))
  }

  def getCollection(db: MongoDatabase, collectionName: String): MongoCollection[Document] = {
    db.getCollection(collectionName)
  }

  def await[A](future: Future[A])(implicit timeout: Duration) = Await.result(future, timeout)

}
