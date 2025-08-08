package com.example.alpaca

import cats.effect.*
import cats.implicits.*
import com.example.alpaca.config.Config
import com.example.alpaca.trading.AlpacaTradingClient
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
      
      // Define the account endpoint
      val accountEndpoint = endpoint
        .get
        .in("api" / "alpaca" / "account")
        .out(jsonBody[AccountResponse])
        .description("Get Alpaca account information")
        .tag("Alpaca")
      
      // Define the server logic
      val accountServerLogic = accountEndpoint.serverLogic { _ =>
        IO.fromFuture(IO(client.getAccount())).map { account =>
          val response = AccountResponse(
            id = account.id,
            accountNumber = account.accountNumber,
            status = account.status,
            currency = account.currency,
            buyingPower = account.buyingPower,
            cash = account.cash,
            portfolioValue = account.portfolioValue,
            patternDayTrader = account.patternDayTrader,
            tradingBlocked = account.tradingBlocked,
            createdAt = account.createdAt
          )
          Right(response)
        }.handleErrorWith { e =>
          logger.error("Error getting account information", e)
          IO.pure(Left(("Failed to get account information", 500)))
        }
      }
      
      // Define the market data endpoint
      val marketDataEndpoint = endpoint
        .get
        .in("api" / "alpaca" / "market-data" / path[String]("symbol"))
        .out(jsonBody[MarketDataResponse])
        .description("Get market data for a given symbol")
        .tag("Alpaca")
      
      // Define the market data server logic
      val marketDataServerLogic = marketDataEndpoint.serverLogic { symbol =>
        val latestTradeFuture = client.getLatestTrade(symbol)
        val latestQuoteFuture = client.getLatestQuote(symbol)
        val barsFuture = client.getBars(symbol, "1Min", limit = Some(5))
        
        (for {
          trade <- IO.fromFuture(IO(latestTradeFuture))
          quote <- IO.fromFuture(IO(latestQuoteFuture))
          bars <- IO.fromFuture(IO(barsFuture))
        } yield {
          val response = MarketDataResponse(
            symbol = symbol,
            latestTrade = trade,
            latestQuote = quote,
            recentBars = bars
          )
          Right(response)
        }).handleErrorWith { e =>
          logger.error(s"Error getting market data for $symbol", e)
          IO.pure(Left(("Failed to get market data", 500)))
        }
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

// Response models for the API
case class AccountResponse(
  id: String,
  accountNumber: String,
  status: String,
  currency: String,
  buyingPower: String,
  cash: String,
  portfolioValue: String,
  patternDayTrader: Boolean,
  tradingBlocked: Boolean,
  createdAt: java.time.Instant
)

case class MarketDataResponse(
  symbol: String,
  latestTrade: com.example.alpaca.models.Trade,
  latestQuote: com.example.alpaca.models.Quote,
  recentBars: List[com.example.alpaca.models.Bar]
)
