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
  private val marketDataUri = uri"https://data.alpaca.markets"
  
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
      .response(asString) // Get raw response first
    
    logger.info(s"🔍 Making account request to: ${request.uri}")
    logger.info(s"🔍 Request headers: ${headers}")
    
    request.send(backend).map { response =>
      logger.info(s"📡 Account response status: ${response.code}")
      logger.info(s"📡 Account response headers: ${response.headers}")
      logger.info(s"📡 Account response body: ${response.body}")
      
      // Try to parse as JSON
      response.body match
        case Right(jsonString) =>
          logger.info(s"📡 Raw JSON response: $jsonString")
          
          // Parse the JSON manually to see what we're getting
          io.circe.parser.parse(jsonString) match
            case Right(json) =>
              logger.info(s"📡 Parsed JSON: ${json.spaces2}")
              
              // Try to decode as Account
              json.as[Account] match
                case Right(account) => 
                  logger.info(s"✅ Successfully parsed account: ${account.id}")
                  account
                case Left(decodeError) =>
                  logger.error(s"❌ JSON decode error: $decodeError")
                  logger.error(s"❌ JSON structure: ${json.spaces2}")
                  throw new RuntimeException(s"Failed to decode account: $decodeError")
            case Left(parseError) =>
              logger.error(s"❌ JSON parse error: $parseError")
              throw new RuntimeException(s"Failed to parse JSON: $parseError")
        case Left(error) => 
          logger.error(s"❌ HTTP error: $error")
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
    // Create JSON object for the order
    val orderJson = io.circe.Json.obj(
      "symbol" -> io.circe.Json.fromString(symbol),
      "qty" -> io.circe.Json.fromString(qty),
      "side" -> io.circe.Json.fromString(side),
      "type" -> io.circe.Json.fromString(orderType),
      "time_in_force" -> io.circe.Json.fromString(timeInForce)
    ).deepMerge(
      limitPrice.map(price => 
        io.circe.Json.obj("limit_price" -> io.circe.Json.fromString(price))
      ).getOrElse(io.circe.Json.obj())
    ).deepMerge(
      stopPrice.map(price => 
        io.circe.Json.obj("stop_price" -> io.circe.Json.fromString(price))
      ).getOrElse(io.circe.Json.obj())
    )
    
    val jsonString = orderJson.noSpaces
    logger.info(s"🔍 Submitting order with JSON: $jsonString")
    
    val request = basicRequest
      .post(baseUri.addPath("v2", "orders"))
      .headers(headers)
      .contentType("application/json")
      .body(jsonString)
      .response(asString)
    
    logger.info(s"🔍 Making order request to: ${request.uri}")
    logger.info(s"🔍 Order request headers: ${headers}")
    
    request.send(backend).map { response =>
      logger.info(s"📡 Order response status: ${response.code}")
      logger.info(s"📡 Order response body: ${response.body}")
      
      response.body match
        case Right(jsonString) =>
          logger.info(s"📡 Raw order response: $jsonString")
          
          io.circe.parser.parse(jsonString) match
            case Right(json) =>
              logger.info(s"📡 Parsed order JSON: ${json.spaces2}")
              
              json.as[Order] match
                case Right(order) => 
                  logger.info(s"✅ Order submitted successfully: ${order.id}")
                  order
                case Left(decodeError) =>
                  logger.error(s"❌ Order JSON decode error: $decodeError")
                  logger.error(s"❌ Order JSON structure: ${json.spaces2}")
                  throw new RuntimeException(s"Failed to decode order: $decodeError")
            case Left(parseError) =>
              logger.error(s"❌ Order JSON parse error: $parseError")
              throw new RuntimeException(s"Failed to parse order JSON: $parseError")
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
    val uri = marketDataUri.addPath("v2", "stocks", symbol, "bars")
    val baseParams = Map("timeframe" -> timeframe)
    val startParam = start.map("start" -> _).toMap
    val endParam = end.map("end" -> _).toMap
    val limitParam = limit.map("limit" -> _.toString).toMap
    val params = baseParams ++ startParam ++ endParam ++ limitParam
    
    logger.info(s"🔍 Making bars request to: ${uri.addParams(params)}")
    
    val request = basicRequest
      .get(uri.addParams(params))
      .headers(headers)
      .response(asJson[Json])
    
    request.send(backend).map { response =>
      logger.info(s"📡 Bars response status: ${response.code}")
      response.body match
        case Right(json) => 
          json.hcursor.downField("bars").as[List[Bar]] match
            case Right(bars) => 
              logger.info(s"✅ Successfully parsed ${bars.length} bars")
              bars
            case Left(error) => 
              logger.error(s"Failed to parse bars: $error")
              throw new RuntimeException(s"Failed to parse bars: $error")
        case Left(error) => 
          logger.error(s"Failed to get bars: $error")
          throw new RuntimeException(s"Failed to get bars: $error")
    }
  
  def getLatestTrade(symbol: String): Future[Trade] =
    val request = basicRequest
      .get(marketDataUri.addPath("v2", "stocks", symbol, "trades", "latest"))
      .headers(headers)
      .response(asString)
    
    logger.info(s"🔍 Making latest trade request to: ${request.uri}")
    
    request.send(backend).map { response =>
      logger.info(s"📡 Latest trade response status: ${response.code}")
      logger.info(s"📡 Latest trade response body: ${response.body}")
      
      response.body match
        case Right(jsonString) =>
          logger.info(s"📡 Raw trade JSON: $jsonString")
          
          io.circe.parser.parse(jsonString) match
            case Right(json) =>
              logger.info(s"📡 Parsed trade JSON: ${json.spaces2}")
              
              json.hcursor.downField("trade").as[Trade] match
                case Right(trade) => 
                  logger.info(s"✅ Successfully got latest trade: $${trade.p}")
                  trade
                case Left(decodeError) =>
                  logger.error(s"❌ Trade JSON decode error: $decodeError")
                  logger.error(s"❌ Trade JSON structure: ${json.spaces2}")
                  throw new RuntimeException(s"Failed to decode trade: $decodeError")
            case Left(parseError) =>
              logger.error(s"❌ Trade JSON parse error: $parseError")
              throw new RuntimeException(s"Failed to parse trade JSON: $parseError")
        case Left(error) => 
          logger.error(s"Failed to get latest trade for $symbol: $error")
          throw new RuntimeException(s"Failed to get latest trade: $error")
    }
  
  def getLatestQuote(symbol: String): Future[Quote] =
    val request = basicRequest
      .get(marketDataUri.addPath("v2", "stocks", symbol, "quotes", "latest"))
      .headers(headers)
      .response(asString)
    
    logger.info(s"🔍 Making latest quote request to: ${request.uri}")
    
    request.send(backend).map { response =>
      logger.info(s"📡 Latest quote response status: ${response.code}")
      logger.info(s"📡 Latest quote response body: ${response.body}")
      
      response.body match
        case Right(jsonString) =>
          logger.info(s"📡 Raw quote JSON: $jsonString")
          
          io.circe.parser.parse(jsonString) match
            case Right(json) =>
              logger.info(s"📡 Parsed quote JSON: ${json.spaces2}")
              
              json.hcursor.downField("quote").as[Quote] match
                case Right(quote) => 
                  logger.info(s"✅ Successfully got latest quote: Bid $${quote.bp} Ask $${quote.ap}")
                  quote
                case Left(decodeError) =>
                  logger.error(s"❌ Quote JSON decode error: $decodeError")
                  logger.error(s"❌ Quote JSON structure: ${json.spaces2}")
                  throw new RuntimeException(s"Failed to decode quote: $decodeError")
            case Left(parseError) =>
              logger.error(s"❌ Quote JSON parse error: $parseError")
              throw new RuntimeException(s"Failed to parse quote JSON: $parseError")
        case Left(error) => 
          logger.error(s"Failed to get latest quote for $symbol: $error")
          throw new RuntimeException(s"Failed to get latest quote: $error")
    }
