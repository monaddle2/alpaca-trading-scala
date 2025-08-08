import com.example.alpaca.config.Config
import com.example.alpaca.trading.AlpacaTradingClient
import com.example.alpaca.models.*
import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import java.time.{Instant, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

object AlpacaTestScript:
  
  def main(args: Array[String]): Unit =
    println("🚀 Starting Alpaca Trading API Test Script")
    println("=" * 50)
    
    // Load configuration from environment variables
    val config = try
      Config.loadFromEnv()
    catch
      case e: IllegalArgumentException =>
        println(s"❌ Configuration error: ${e.getMessage}")
        println("Please set the following environment variables:")
        println("  ALPACA_KEY - Your Alpaca API key")
        println("  ALPACA_SECRET - Your Alpaca secret key")
        println("  ALPACA_BASE_URL - Alpaca API base URL (optional, defaults to paper trading)")
        System.exit(1)
        Config.loadFromEnv() // This will never be reached, but satisfies the type checker
    
    println(s"✅ Configuration loaded successfully")
    println(s"   Base URL: ${config.baseUrl}")
    println(s"   Paper Trading: ${config.isPaper}")
    println()
    
    // Create trading client
    val client = AlpacaTradingClient(config)
    
    // Run all tests
    runAllTests(client)
  
  def runAllTests(client: AlpacaTradingClient): Unit =
    println("🧪 Running Alpaca API Tests")
    println("=" * 50)
    
    // Test 1: Get Account Information
    testAccountInfo(client)
    
    // Test 2: Get Market Data
    testMarketData(client)
    
    // Test 3: Get Orders (read-only)
    testGetOrders(client)
    
    // Test 4: Get Positions (read-only)
    testGetPositions(client)
    
    // Test 5: Submit and Cancel Order (if paper trading)
    if client.config.isPaper then
      testOrderOperations(client)
    else
      println("⚠️  Skipping order operations for live trading (safety)")
    
    println("\n✅ All tests completed!")
  
  def testAccountInfo(client: AlpacaTradingClient): Unit =
    println("\n📊 Test 1: Account Information")
    println("-" * 30)
    
    client.getAccount().onComplete {
      case Success(account) =>
        println(s"✅ Account retrieved successfully")
        println(s"   Account ID: ${account.id}")
        println(s"   Status: ${account.status}")
        println(s"   Currency: ${account.currency}")
        println(s"   Buying Power: ${account.buyingPower}")
        println(s"   Cash: ${account.cash}")
        println(s"   Portfolio Value: ${account.portfolioValue}")
        println(s"   Pattern Day Trader: ${account.patternDayTrader}")
        println(s"   Trading Blocked: ${account.tradingBlocked}")
        println(s"   Created At: ${account.createdAt}")
      
      case Failure(error) =>
        println(s"❌ Failed to get account: ${error.getMessage}")
    }
    
    // Wait for completion
    Thread.sleep(2000)
  
  def testMarketData(client: AlpacaTradingClient): Unit =
    println("\n📈 Test 2: Market Data")
    println("-" * 30)
    
    val symbols = List("AAPL", "MSFT", "GOOGL")
    
    symbols.foreach { symbol =>
      println(s"\n📊 Getting data for $symbol:")
      
      // Get latest trade
      client.getLatestTrade(symbol).onComplete {
        case Success(trade) =>
          println(s"   Latest Trade: $${trade.p} at ${trade.t}")
        case Failure(error) =>
          println(s"   ❌ Failed to get latest trade: ${error.getMessage}")
      }
      
      // Get latest quote
      client.getLatestQuote(symbol).onComplete {
        case Success(quote) =>
          println(s"   Latest Quote: Bid $${quote.bp} Ask $${quote.ap}")
        case Failure(error) =>
          println(s"   ❌ Failed to get latest quote: ${error.getMessage}")
      }
      
      // Get recent bars (last 5 minutes)
      val endTime = Instant.now()
      val startTime = endTime.minusSeconds(300) // 5 minutes ago
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
      
      client.getBars(
        symbol = symbol,
        timeframe = "1Min",
        start = Some(startTime.atOffset(ZoneOffset.UTC).format(formatter)),
        end = Some(endTime.atOffset(ZoneOffset.UTC).format(formatter)),
        limit = Some(5)
      ).onComplete {
        case Success(bars) =>
          println(s"   Recent Bars: ${bars.length} bars retrieved")
          bars.take(3).foreach { bar =>
            println(s"     ${bar.t}: O:$$${bar.o} H:$$${bar.h} L:$$${bar.l} C:$$${bar.c} V:${bar.v}")
          }
        case Failure(error) =>
          println(s"   ❌ Failed to get bars: ${error.getMessage}")
      }
      
      Thread.sleep(1000) // Rate limiting
    }
    
    Thread.sleep(3000)
  
  def testGetOrders(client: AlpacaTradingClient): Unit =
    println("\n📋 Test 3: Get Orders")
    println("-" * 30)
    
    // Get all orders
    client.getOrders(limit = Some(10)).onComplete {
      case Success(orders) =>
        println(s"✅ Retrieved ${orders.length} orders")
        orders.take(3).foreach { order =>
          println(s"   Order ${order.id}: ${order.symbol} ${order.side} ${order.qty.getOrElse("N/A")} @ ${order.orderType}")
          println(s"     Status: ${order.status}, Created: ${order.createdAt}")
        }
        if orders.length > 3 then
          println(s"   ... and ${orders.length - 3} more orders")
      
      case Failure(error) =>
        println(s"❌ Failed to get orders: ${error.getMessage}")
    }
    
    Thread.sleep(2000)
  
  def testGetPositions(client: AlpacaTradingClient): Unit =
    println("\n💼 Test 4: Get Positions")
    println("-" * 30)
    
    client.getPositions().onComplete {
      case Success(positions) =>
        println(s"✅ Retrieved ${positions.length} positions")
        positions.foreach { position =>
          println(s"   ${position.symbol}: ${position.qty} shares @ $${position.avgEntryPrice}")
          println(s"     Market Value: $${position.marketValue}, Unrealized P&L: $${position.unrealizedPl}")
        }
        if positions.isEmpty then
          println("   No open positions")
      
      case Failure(error) =>
        println(s"❌ Failed to get positions: ${error.getMessage}")
    }
    
    Thread.sleep(2000)
  
  def testOrderOperations(client: AlpacaTradingClient): Unit =
    println("\n🛒 Test 5: Order Operations (Paper Trading)")
    println("-" * 30)
    
    // Submit a limit order for a small amount
    val testSymbol = "AAPL"
    val testQty = "1"
    val testPrice = "150.00" // Set a limit price that's unlikely to execute immediately
    
    println(s"📝 Submitting limit order: Buy $testQty $testSymbol @ $${testPrice}")
    
    client.submitOrder(
      symbol = testSymbol,
      qty = testQty,
      side = "buy",
      orderType = "limit",
      limitPrice = Some(testPrice)
    ).onComplete {
      case Success(order) =>
        println(s"✅ Order submitted successfully!")
        println(s"   Order ID: ${order.id}")
        println(s"   Status: ${order.status}")
        println(s"   Symbol: ${order.symbol}")
        println(s"   Side: ${order.side}")
        println(s"   Quantity: ${order.qty}")
        println(s"   Type: ${order.orderType}")
        println(s"   Limit Price: ${order.limitPrice}")
        println(s"   Created At: ${order.createdAt}")
        
        // Cancel the order after a short delay
        Thread.sleep(2000)
        println(s"🔄 Cancelling order ${order.id}...")
        
        client.cancelOrder(order.id).onComplete {
          case Success(_) =>
            println(s"✅ Order ${order.id} cancelled successfully")
          case Failure(error) =>
            println(s"❌ Failed to cancel order: ${error.getMessage}")
        }
      
      case Failure(error) =>
        println(s"❌ Failed to submit order: ${error.getMessage}")
    }
    
    Thread.sleep(5000)
  
  // Extension method to access config
  extension (client: AlpacaTradingClient)
    def config: com.example.alpaca.config.AlpacaConfig = 
      // This is a workaround since the config is private in the client
      // In a real implementation, you might want to expose this or pass it separately
      Config.loadFromEnv()
