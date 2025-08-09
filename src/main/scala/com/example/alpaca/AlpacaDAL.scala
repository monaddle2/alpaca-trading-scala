package com.example.alpaca

import cats.effect.*
import com.example.alpaca.trading.AlpacaTradingClient
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.ExecutionContext.Implicits.global

class AlpacaDAL(client: AlpacaTradingClient) extends LazyLogging:
  
  /**
   * Get account information from Alpaca API
   */
  def getAccount(): IO[Either[Unit, AccountResponse]] =
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
      IO.pure(Left(()))
    }

  /**
   * Get market data for a given symbol with optional date range
   */
  def getMarketData(symbol: String, startOpt: Option[String], endOpt: Option[String]): IO[Either[Unit, MarketDataResponse]] =
    // Default to Friday, August 8th, 2025 if no dates provided
    val defaultStart = "2025-08-08T00:00:00Z"
    val defaultEnd = "2025-08-08T23:59:59Z"
    
    val start = startOpt.getOrElse(defaultStart)
    val end = endOpt.getOrElse(defaultEnd)
    
    val latestTradeFuture = client.getLatestTrade(symbol)
    val latestQuoteFuture = client.getLatestQuote(symbol)
    val barsFuture = client.getBars(symbol, "1Min", start = Some(start), end = Some(end), limit = Some(100))
    
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
      IO.pure(Left(()))
    }

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
