package com.example.alpaca

import com.example.alpaca.config.Config
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.math.BigDecimal

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
