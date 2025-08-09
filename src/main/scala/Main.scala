package com.example.alpaca

import cats.effect.*
import cats.implicits.*
import com.example.alpaca.config.Config
import com.example.alpaca.trading.AlpacaTradingClient
import com.example.alpaca.{AlpacaDAL, AccountResponse, MarketDataResponse}
import com.typesafe.scalalogging.LazyLogging
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import sttp.tapir.path
import org.http4s.server.middleware.Logger
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import io.circe.generic.auto.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}
import cats.effect.unsafe.implicits.global
import com.comcast.ip4s.*

@main def main(): Unit =
  val app = new AlpacaTradingWebServer()
  app.run()

class AlpacaTradingWebServer extends LazyLogging:
  def run(): Unit =
    logger.info("Starting Alpaca Trading Web Server")
    
    try
      val config = Config.load()
      val client = new AlpacaTradingClient(config)
      val dal = new AlpacaDAL(client)
      
      // Define the account endpoint
      val accountEndpoint = endpoint
        .get
        .in("api" / "alpaca" / "account")
        .out(jsonBody[AccountResponse])
        .description("Get Alpaca account information")
        .tag("Alpaca")
      
      // Define the account server logic
      val accountServerLogic = accountEndpoint.serverLogic { _ =>
        dal.getAccount()
      }
      
      // Define the market data endpoint
      val marketDataEndpoint = endpoint
        .get
        .in("api" / "alpaca" / "market-data" / path[String]("symbol"))
        .in(query[Option[String]]("start").description("Start date in ISO format (e.g., 2025-08-09T00:00:00Z)"))
        .in(query[Option[String]]("end").description("End date in ISO format (e.g., 2025-08-09T23:59:59Z)"))
        .out(jsonBody[MarketDataResponse])
        .description("Get market data for a given symbol with optional date range")
        .tag("Alpaca")
      
      // Define the market data server logic
      val marketDataServerLogic = marketDataEndpoint.serverLogic { case (symbol, startOpt, endOpt) =>
        dal.getMarketData(symbol, startOpt, endOpt)
      }
      
      // Create Swagger documentation
      val swaggerEndpoints = SwaggerInterpreter()
        .fromEndpoints[IO](List(accountEndpoint, marketDataEndpoint), "Alpaca Trading API", "1.0.0")
      
      val swaggerRoutes = Http4sServerInterpreter[IO]()
        .toRoutes(swaggerEndpoints)
      
      // Create the main routes
      val accountRoutes = Http4sServerInterpreter[IO]()
        .toRoutes(accountServerLogic)
      val marketDataRoutes = Http4sServerInterpreter[IO]()
        .toRoutes(marketDataServerLogic)
      val routes = accountRoutes <+> marketDataRoutes
      
      // Combine routes
      val allRoutes = routes <+> swaggerRoutes
      
      // Add logging middleware
      val loggedRoutes = Logger.httpApp[IO](true, true)(allRoutes.orNotFound)
      
      // Start the server
      val server = org.http4s.ember.server.EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(loggedRoutes)
        .build
      
      logger.info("Server starting on http://localhost:8080")
      logger.info("Swagger UI available at http://localhost:8080/docs")
      
      // Run the server
      server.use { _ =>
        IO.never
      }.unsafeRunSync()
      
    catch
      case e: Exception =>
        logger.error("Error starting web server", e)
        throw e
