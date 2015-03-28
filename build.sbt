name := "scala-basics"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4"

testOptions in Test += Tests.Argument("-C", "io.scalac.presentation.CourseReporter")

//testOptions in Test += Tests.Argument("-h", "dupa")