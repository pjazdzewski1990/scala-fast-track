package io.scalac.presentation

import java.nio.charset.StandardCharsets
import java.nio.file.{Paths, Files}

import org.scalatest._
import org.scalatest.events._

import scala.io.Source

class CourseReporter extends Reporter {

  private var tests = List[TestSucceeded]()

  def getStyle() =
    """
      |.accordion-toggle {cursor: pointer; font-style: oblique; font-weight: normal }
      |.accordion-toggle.main {cursor: pointer; font-size: 120%; font-weight: bold }
      |.accordion-content {display: none;}
      |.accordion-content.default {display: block;}
      |
      |.code {background-color: grey;}
    """.stripMargin

  def createAccordionScript() =
    """
      |  $(document).ready(function($) {
      |    $('#accordion').find('.accordion-toggle').click(function(){
      |
      |      //Expand or collapse this panel
      |      $(this).next().slideToggle('fast');
      |
      |    });
      |  });
    """.stripMargin

  private val pathPrefix = "src/test/scala/eu/gruchala/scala/language/"
  private val sourceMapping = Map(
    "ProcessingFutures.scala" -> "futures/ProcessingFutures.scala",
    "ExceptionsOverFutures.scala" -> "futures/ExceptionsOverFutures.scala"
  )
  def getCode(file: String, line: Int) =  {
    val fullPath = pathPrefix + sourceMapping.getOrElse(file, file)
    val fileContents = Source.fromFile(fullPath).getLines.toList.slice(line-2, line + 5) //TODO
    <div>
      {fileContents.map( s =>
        <div class="code">{s}</div>
      )}
    </div>
  }

  def getSource(test: TestSucceeded) = {
    test.location.map { //TODO
      case LineInFile(lineNumber: Int, fileName: String) => getCode(fileName, lineNumber)
    } getOrElse("Code is missing :(")
  }

  def showSingle(results: List[TestSucceeded]) = {
    results.reverse.map(tr => {
      <div>
        <h4 class="accordion-toggle">{tr.testText}</h4>
        <div class="accordion-content">
          <p>{getSource(tr)}</p>
        </div>
      </div>
    })
  }

  def showResults() = {
    val groupedTestResults = tests.groupBy(_.suiteName)
    groupedTestResults.map { case (suite, tests) =>
      <div>
        <h4 class="accordion-toggle main">{suite}</h4>
        <div class="accordion-content">
          <p>{showSingle(tests)}</p>
        </div>
      </div>
    }
  }

  def createBody() = {
    <div id="accordion">
      {showResults()}
    </div>
  }

  def createHtml() = {
    <html>
      <head>
        <meta charset="utf-8"/>
        <title>title</title>
        <script src="https://code.jquery.com/jquery-2.1.3.js"></script>
        <style>
          {getStyle()}
        </style>
        <script type="text/javascript">
          {createAccordionScript()}
    </script>
      </head>
      <body>
        {createBody()}
      </body>
    </html>
  }

  def createReport(): Unit = {
    Files.write(Paths.get("course.html"), createHtml().toString().getBytes(StandardCharsets.UTF_8))
  }

  def apply(event: Event) {
    event match {
      case e: TestSucceeded =>
        tests = e :: tests
      case e: RunCompleted =>
        println(s"End ${e}")
        createReport()
      case e =>
        println(s"  event ==== ${e}")
    }
  }
}
