package com.example.alpaca

import com.example.alpaca.config.Config
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.math.BigDecimal
import com.example.alpaca.CrossoverType

class AlpacaTradingSpec extends AnyFunSuite with Matchers:
  
  test("Config should load successfully") {
    // This test will fail if the config file is missing or malformed
    val config = Config.load()
    config.apiKey should not be empty
    config.secretKey should not be empty
    config.baseUrl should not be empty
  }
  
  test("Config should support paper trading by default") {
    val config = Config.load()
    config.isPaper shouldBe true
  }
  
  test("Config should have valid base URL") {
    val config = Config.load()
    config.baseUrl should startWith("https://")
  }

  test("Moving average calculation should work correctly") {
    // Test data: prices for 10 periods
    val prices = List(
      BigDecimal("100.0"),
      BigDecimal("101.0"), 
      BigDecimal("102.0"),
      BigDecimal("103.0"),
      BigDecimal("104.0"),
      BigDecimal("105.0"),
      BigDecimal("106.0"),
      BigDecimal("107.0"),
      BigDecimal("108.0"),
      BigDecimal("109.0")
    )
    
    // Test with N=3
    val n = 3
    val expectedMA3 = List(
      None, // index 0: not enough data
      None, // index 1: not enough data  
      Some(BigDecimal("101.0")), // (100+101+102)/3 = 101.0
      Some(BigDecimal("102.0")), // (101+102+103)/3 = 102.0
      Some(BigDecimal("103.0")), // (102+103+104)/3 = 103.0
      Some(BigDecimal("104.0")), // (103+104+105)/3 = 104.0
      Some(BigDecimal("105.0")), // (104+105+106)/3 = 105.0
      Some(BigDecimal("106.0")), // (105+106+107)/3 = 106.0
      Some(BigDecimal("107.0")), // (106+107+108)/3 = 107.0
      Some(BigDecimal("108.0"))  // (107+108+109)/3 = 108.0
    )
    
    // Create a test instance to access the package-private method
    val testDAL = new AlpacaDAL(null) // We don't need the client for this test
    val result = testDAL.calculateMovingAverage(prices, n)
    
    result should have length prices.length
    result shouldBe expectedMA3
  }

  test("Moving average calculation should handle edge cases") {
    val testDAL = new AlpacaDAL(null)
    
    // Test with empty list
    val emptyResult = testDAL.calculateMovingAverage(List.empty[BigDecimal], 3)
    emptyResult shouldBe List.empty
    
    // Test with N=0
    val prices = List(BigDecimal("100.0"), BigDecimal("101.0"))
    val zeroNResult = testDAL.calculateMovingAverage(prices, 0)
    zeroNResult shouldBe List(None, None)
    
    // Test with N=1 (should return the original prices)
    val n1Result = testDAL.calculateMovingAverage(prices, 1)
    n1Result shouldBe List(Some(BigDecimal("100.0")), Some(BigDecimal("101.0")))
  }

  test("Moving average calculation should handle decimal precision correctly") {
    val prices = List(
      BigDecimal("100.50"),
      BigDecimal("101.25"),
      BigDecimal("102.75")
    )
    
    val testDAL = new AlpacaDAL(null)
    val result = testDAL.calculateMovingAverage(prices, 3)
    
    // Expected: (100.50 + 101.25 + 102.75) / 3 = 304.50 / 3 = 101.50
    result shouldBe List(None, None, Some(BigDecimal("101.50")))
  }

  test("Crossover detector should identify golden and death crosses correctly") {
    val testDAL = new AlpacaDAL(null)
    
    // Create a 20-bar fixture with carefully designed crossover patterns
    // This creates a scenario where:
    // - Fast MA (3-period) crosses above slow MA (5-period) at bar 7 (Golden Cross)
    // - Fast MA crosses below slow MA at bar 12 (Death Cross)
    val prices: List[BigDecimal] = List(
      BigDecimal("110.0"), //  0
      BigDecimal("109.0"), //  1
      BigDecimal("108.0"), //  2
      BigDecimal("107.0"), //  3
      BigDecimal("106.0"), //  4
      BigDecimal("107.0"), //  5
      BigDecimal("108.0"), //  6
      BigDecimal("109.0"), //  7  <-- Golden Cross (SMA3 crosses above SMA5)
      BigDecimal("110.0"), //  8
      BigDecimal("111.0"), //  9
      BigDecimal("110.0"), // 10
      BigDecimal("109.0"), // 11
      BigDecimal("108.0"), // 12  <-- Death Cross (SMA3 crosses below SMA5)
      BigDecimal("107.0"), // 13
      BigDecimal("106.0"), // 14
      BigDecimal("105.0"), // 15
      BigDecimal("104.0"), // 16
      BigDecimal("103.0"), // 17
      BigDecimal("102.0"), // 18
      BigDecimal("101.0")  // 19
    )
    
    // Create timestamps for the 20 bars (1-minute intervals starting from a fixed time)
    val baseTime = java.time.Instant.parse("2025-08-09T10:00:00Z")
    val timestamps = (0 until 20).map(i => baseTime.plusSeconds(i * 60)).toList
    
    // Calculate moving averages
    val fastMA = testDAL.calculateMovingAverage(prices, 3) // 3-period MA
    val slowMA = testDAL.calculateMovingAverage(prices, 5) // 5-period MA
    
    // Debug: Print the moving averages to understand what's happening
    println("Fast MA (3-period):")
    fastMA.zipWithIndex.foreach { case (ma, i) => println(s"  Bar $i: $ma") }
    
    println("Slow MA (5-period):")
    slowMA.zipWithIndex.foreach { case (ma, i) => println(s"  Bar $i: $ma") }
    
    // Detect crossovers
    val crossovers = testDAL.detectCrossovers(fastMA, slowMA, timestamps)
    
    println(s"Found ${crossovers.length} crossovers:")
    crossovers.foreach { signal =>
      println(s"  ${signal.crossoverType} cross at ${signal.timestamp}")
    }
    
    // Verify the results - we expect exactly 2 crossovers
    crossovers should have length 2
    
    // Check golden cross at bar 7 (timestamp should be baseTime + 7*60 seconds)
    val goldenCross = crossovers.find(_.crossoverType == CrossoverType.Golden)
    goldenCross shouldBe defined
    goldenCross.get.timestamp shouldBe baseTime.plusSeconds(7 * 60)
    
    // Check death cross at bar 12 (timestamp should be baseTime + 12*60 seconds)
    val deathCross = crossovers.find(_.crossoverType == CrossoverType.Death)
    deathCross shouldBe defined
    deathCross.get.timestamp shouldBe baseTime.plusSeconds(12 * 60)
  }

  test("Crossover detector should handle edge cases") {
    val testDAL = new AlpacaDAL(null)
    
    // Test with empty lists
    val emptyCrossovers = testDAL.detectCrossovers(List.empty, List.empty, List.empty)
    emptyCrossovers shouldBe List.empty
    
    // Test with mismatched lengths
    val prices = List(BigDecimal("100.0"), BigDecimal("101.0"))
    val timestamps = List(java.time.Instant.now(), java.time.Instant.now())
    val fastMA = testDAL.calculateMovingAverage(prices, 2)
    val slowMA = testDAL.calculateMovingAverage(prices, 2)
    
    // Test with different length timestamps
    val mismatchedCrossovers = testDAL.detectCrossovers(fastMA, slowMA, timestamps.take(1))
    mismatchedCrossovers shouldBe List.empty
    
    // Test with insufficient data (less than 2 points)
    val singlePrice = List(BigDecimal("100.0"))
    val singleTimestamp = List(java.time.Instant.now())
    val singleFastMA = testDAL.calculateMovingAverage(singlePrice, 1)
    val singleSlowMA = testDAL.calculateMovingAverage(singlePrice, 1)
    
    val insufficientCrossovers = testDAL.detectCrossovers(singleFastMA, singleSlowMA, singleTimestamp)
    insufficientCrossovers shouldBe List.empty
  }
