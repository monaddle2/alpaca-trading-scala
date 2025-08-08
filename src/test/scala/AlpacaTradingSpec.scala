package com.example.alpaca

import com.example.alpaca.config.Config
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

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
