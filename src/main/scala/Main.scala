package com.example.alpaca

import cats.effect.*
import com.example.alpaca.server.AlpacaServer
import com.typesafe.scalalogging.LazyLogging
import cats.effect.unsafe.implicits.global

@main def main(): Unit =
  val app = new AlpacaTradingWebServer()
  app.run()

class AlpacaTradingWebServer extends LazyLogging:
  def run(): Unit =
    logger.info("Starting Alpaca Trading Web Server")
    
    try
      val server = new AlpacaServer()
      
      // Create the app and server resource
      val serverResource = for
        app <- Resource.eval(server.createApp())
        serverRes <- server.createServerResource(app)
      yield serverRes
      
      logger.info("Server starting on http://localhost:8080")
      logger.info("Swagger UI available at http://localhost:8080/docs")
      
      // Run the server
      serverResource.use { _ =>
        IO.never
      }.unsafeRunSync()
      
    catch
      case e: Exception =>
        logger.error("Error starting web server", e)
        throw e
