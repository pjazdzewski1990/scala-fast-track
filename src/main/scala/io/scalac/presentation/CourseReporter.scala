package io.scalac.presentation

import org.scalatest._
import org.scalatest.events._

class CourseReporter extends Reporter {

  private var tests = List[Event]() ///TODO: restrict types to something with `suiteName` property

  def apply(event: Event) {
    event match {
      case e: TestSucceeded =>
        tests = event :: tests
      case e: TestPending =>
        tests = event :: tests
      case e: RunCompleted =>
        new CourseFormatter(tests).createReport()
      case e => //do nothing
    }
  }
}
