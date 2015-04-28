package io.scalac.presentation

import java.nio.charset.StandardCharsets
import java.nio.file.{Paths, Files}

import org.scalatest.events.{TestPending, Event, LineInFile, TestSucceeded}

import scala.io.Source
import scala.xml.Elem

class CourseFormatter(tests: List[Event]) {

  val progressBarStyle =
    """
      |progress[value] {
      |  /* Reset the default appearance */
      |  -webkit-appearance: none;
      |   appearance: none;
      |
      |  width: 250px;
      |  height: 20px;
      |}
      |
      |progress[value]::-webkit-progress-value {
      |  background-image:
      |	   -webkit-linear-gradient(-45deg,
      |	                           transparent 33%, rgba(0, 0, 0, .1) 33%,
      |	                           rgba(0,0, 0, .1) 66%, transparent 66%),
      |	   -webkit-linear-gradient(top,
      |	                           rgba(255, 255, 255, .25),
      |	                           rgba(0, 0, 0, .25)),
      |	   -webkit-linear-gradient(left, #333, #f44);
      |
      |    border-radius: 2px;
      |    background-size: 35px 20px, 100% 100%, 100% 100%;
      |}
    """.stripMargin

  def getStyle() =
    s"""
       |.accordion-toggle {cursor: pointer; font-style: oblique; font-weight: normal }
       |.accordion-toggle.main {cursor: pointer; font-size: 120%; font-weight: bold }
       |.accordion-content {display: none;}
       |.accordion-content.default {display: block;}
       |
       |.code {background-color: #6a4;}
       |.pending {background-color: #f44;}
       |
       |$progressBarStyle
       |
       |#content { margin-right: auto; margin-left: auto; width: 50%; }
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

  def getCode(file: String, line: Int) =  { //TODO: find a better way of extracting source code
  val fullPath = pathPrefix + sourceMapping.getOrElse(file, file)
    val wholeFile = Source.fromFile(fullPath).getLines
    val source = wholeFile.drop(line-1).takeWhile(!_.contains("//~"))

    <div>
      {source.map( s => //TODO:get spaces right
        <div class="code">{s}</div>
      )}
    </div>
  }

  def getSource(test: TestSucceeded): Elem = {
    test.location.flatMap {
      case LineInFile(lineNumber: Int, fileName: String) => Some(getCode(fileName, lineNumber))
      case _ => Option.empty[Elem]
    } getOrElse(<span>Code is missing :(</span>)
  }

  def showSingle(results: List[Event]) = {
    results.reverse.map {
      case tr : TestSucceeded =>
        <div>
          <h4 class="accordion-toggle">{tr.testText}</h4>
          <div class="accordion-content">
            {getSource(tr)}
          </div>
        </div>
      case tr : TestPending =>
        <div>
          <h4 class="accordion-toggle">TODO: {tr.testText}</h4>
          <div class="accordion-content">
            <p class="pending">PENDING EXCERCISE</p>
          </div>
        </div>
      case _ => <div></div>
    }
  }

  def showResults() = {
    val groupedTestResults = tests.groupBy(x => x match {
      case e : TestSucceeded => e.suiteName
      case e : TestPending => e.suiteName
      case _ => throw new IllegalArgumentException("Unexpected event type")
    })

    groupedTestResults.map { case (suite, tests) =>
      <div>
        <h4 class="accordion-toggle main">{suite}</h4>
        <div class="accordion-content">
          <p>{showSingle(tests)}</p>
        </div>
      </div>
    }
  }

  def showProgress() = {
    val all = tests.length
    val completed = tests.count {
      case e : TestSucceeded => true
      case _ => false
    }
    <div>
      <progress value={ ""+completed } max={ ""+all }></progress>
      {completed}/{all}
    </div>
  }

  def showHeader() =
    <div>
      <span>
        <a href="http://www.scalac.io/"><img src="logo.jpg"></img></a>
      </span>
      <h1>Welcome to Scala!</h1>
      <p>This is a self-paced course designed to help you learn Scala</p>
      <p>
        We learn best by doing so grab
        <a href="https://github.com/leszekgruchala/scala-fast-track">the source</a>
        and improve your skill!
      </p>
      {showProgress()}
    </div>

  def createBody() = {
    <div id="content">
      {showHeader()}
      <div id="accordion">
        {showResults()}
      </div>
    </div>
  }

  def createHtml() = {
    <html>
      <head>
        <meta charset="utf-8"/>
        <title>Scala Course</title>
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
}
