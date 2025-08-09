ThisBuild / scalaVersion := "3.3.6"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"

lazy val root = (project in file("."))
  .settings(
    name := "alpaca-trading-scala",
    libraryDependencies ++= Seq(
      // HTTP client for Alpaca API
      "com.softwaremill.sttp.client3" %% "core" % "3.9.3",
      "com.softwaremill.sttp.client3" %% "circe" % "3.9.3",
      "com.softwaremill.sttp.client3" %% "async-http-client-backend" % "3.9.3",
      
      // JSON parsing
      "io.circe" %% "circe-core" % "0.14.6",
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-parser" % "0.14.6",
      
      // Logging
      "ch.qos.logback" % "logback-classic" % "1.4.11",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      
      // Configuration
      "com.typesafe" % "config" % "1.4.2",
      
      // Web server
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.9.10",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.9.10",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.9.10",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.9.10",
      "org.http4s" %% "http4s-ember-server" % "0.23.24",
      "org.http4s" %% "http4s-dsl" % "0.23.24",
      
      // Testing
      "org.scalatest" %% "scalatest" % "3.2.17" % Test,
      "org.scalatestplus" %% "scalacheck-1-17" % "3.2.17.0" % Test
    ),
    
    // Enable better error messages
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Xfatal-warnings",
      "-Xmax-inlines:64"
    )
  )
