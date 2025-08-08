package com.example.alpaca.trading

import com.example.alpaca.config.AlpacaConfig
import com.example.alpaca.models.*
import sttp.client3.*
import sttp.client3.circe.*
import sttp.model.Uri
import io.circe.*
import io.circe.parser.*
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure}

class AlpacaTradingClient(config: AlpacaConfig)(using ExecutionContext) extends LazyLogging:
  
  private val backend = HttpClientFutureBackend()
  private val baseUri = uri"${config.baseUrl}"
  
  // Headers for authentication
  private val headers = Map(
    "APCA-API-KEY-ID" -> config.apiKey,
    "APCA-API-SECRET-KEY" -> config.secretKey
  )
  
  // Account operations
  def getAccount(): Future[Account] =
    val request = basicRequest
      .get(baseUri.addPath("v2", "account"))
      .headers(headers)
      .response(asJson[Account])
    
    request.send(backend).map { response =>
      response.body match
        case Right(account) => account
        case Left(error) => 
          logger.error(s"Failed to get account: $error")
          throw new RuntimeException(s"Failed to get account: $error")
    }
  
  // Order operations
  def getOrders(status: Option[String] = None, limit: Option[Int] = None): Future[List[Order]] =
    val uri = baseUri.addPath("v2", "orders")
    val params = Map.empty[String, String] ++
      status.map("status" -> _) ++
      limit.map("limit" -> _.toString)
    
    val request = basicRequest
      .get(uri.addParams(params))
      .headers(headers)
      .response(asJson[List[Order]])
    
    request.send(backend).map { response =>
      response.body match
        case Right(orders) => orders
        case Left(error) => 
          logger.error(s"Failed to get orders: $error")
          throw new RuntimeException(s"Failed to get orders: $error")
    }
  
  def submitOrder(
    symbol: String,
    qty: String,
    side: String,
    orderType: String,
    timeInForce: String = "day",
    limitPrice: Option[String] = None,
    stopPrice: Option[String] = None
  ): Future[Order] =
    val orderData = Map(
      "symbol" -> symbol,
      "qty" -> qty,
      "side" -> side,
      "type" -> orderType,
      "time_in_force" -> timeInForce
    ) ++
      limitPrice.map("limit_price" -> _) ++
      stopPrice.map("stop_price" -> _)
    
    val request = basicRequest
      .post(baseUri.addPath("v2", "orders"))
      .headers(headers)
      .body(orderData)
      .response(asJson[Order])
    
    request.send(backend).map { response =>
      response.body match
        case Right(order) => 
          logger.info(s"Order submitted: ${order.id}")
          order
        case Left(error) => 
          logger.error(s"Failed to submit order: $error")
          throw new RuntimeException(s"Failed to submit order: $error")
    }
  
  def cancelOrder(orderId: String): Future[Unit] =
    val request = basicRequest
      .delete(baseUri.addPath("v2", "orders", orderId))
      .headers(headers)
    
    request.send(backend).map { response =>
      if response.code.isSuccess then
        logger.info(s"Order $orderId cancelled successfully")
      else
        logger.error(s"Failed to cancel order $orderId: ${response.statusText}")
        throw new RuntimeException(s"Failed to cancel order: ${response.statusText}")
    }
  
  // Position operations
  def getPositions(): Future[List[Position]] =
    val request = basicRequest
      .get(baseUri.addPath("v2", "positions"))
      .headers(headers)
      .response(asJson[List[Position]])
    
    request.send(backend).map { response =>
      response.body match
        case Right(positions) => positions
        case Left(error) => 
          logger.error(s"Failed to get positions: $error")
          throw new RuntimeException(s"Failed to get positions: $error")
    }
  
  def getPosition(symbol: String): Future[Position] =
    val request = basicRequest
      .get(baseUri.addPath("v2", "positions", symbol))
      .headers(headers)
      .response(asJson[Position])
    
    request.send(backend).map { response =>
      response.body match
        case Right(position) => position
        case Left(error) => 
          logger.error(s"Failed to get position for $symbol: $error")
          throw new RuntimeException(s"Failed to get position: $error")
    }
  
  def closePosition(symbol: String): Future[Order] =
    val request = basicRequest
      .delete(baseUri.addPath("v2", "positions", symbol))
      .headers(headers)
      .response(asJson[Order])
    
    request.send(backend).map { response =>
      response.body match
        case Right(order) => 
          logger.info(s"Position closed for $symbol: ${order.id}")
          order
        case Left(error) => 
          logger.error(s"Failed to close position for $symbol: $error")
          throw new RuntimeException(s"Failed to close position: $error")
    }
  
  // Market data operations
  def getBars(
    symbol: String, 
    timeframe: String, 
    start: Option[String] = None,
    end: Option[String] = None,
    limit: Option[Int] = None
  ): Future[List[Bar]] =
    val uri = baseUri.addPath("v2", "stocks", symbol, "bars")
    val baseParams = Map("timeframe" -> timeframe)
    val startParam = start.map("start" -> _).toMap
    val endParam = end.map("end" -> _).toMap
    val limitParam = limit.map("limit" -> _.toString).toMap
    val params = baseParams ++ startParam ++ endParam ++ limitParam
    
    val request = basicRequest
      .get(uri.addParams(params))
      .headers(headers)
      .response(asJson[Json])
    
    request.send(backend).map { response =>
      response.body match
        case Right(json) => 
          json.hcursor.downField("bars").as[List[Bar]] match
            case Right(bars) => bars
            case Left(error) => 
              logger.error(s"Failed to parse bars: $error")
              throw new RuntimeException(s"Failed to parse bars: $error")
        case Left(error) => 
          logger.error(s"Failed to get bars: $error")
          throw new RuntimeException(s"Failed to get bars: $error")
    }
  
  def getLatestTrade(symbol: String): Future[Trade] =
    val request = basicRequest
      .get(baseUri.addPath("v2", "stocks", symbol, "trades", "latest"))
      .headers(headers)
      .response(asJson[Trade])
    
    request.send(backend).map { response =>
      response.body match
        case Right(trade) => trade
        case Left(error) => 
          logger.error(s"Failed to get latest trade for $symbol: $error")
          throw new RuntimeException(s"Failed to get latest trade: $error")
    }
  
  def getLatestQuote(symbol: String): Future[Quote] =
    val request = basicRequest
      .get(baseUri.addPath("v2", "stocks", symbol, "quotes", "latest"))
      .headers(headers)
      .response(asJson[Quote])
    
    request.send(backend).map { response =>
      response.body match
        case Right(quote) => quote
        case Left(error) => 
          logger.error(s"Failed to get latest quote for $symbol: $error")
          throw new RuntimeException(s"Failed to get latest quote: $error")
    }
