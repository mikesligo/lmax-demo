ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.0"

lazy val root = (project in file("."))
  .settings(
    name := "lmax-demo",
    idePackagePrefix := Some("com.demo.lmax")
  )

libraryDependencies += "com.lmax" % "disruptor" % "3.3.6"