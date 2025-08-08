package com.example.alpaca.config

import com.typesafe.config.ConfigFactory
import scala.util.Try

case class AlpacaConfig(
  apiKey: String,
  secretKey: String,
  baseUrl: String,
  isPaper: Boolean = true
)

object Config:
  private val config = ConfigFactory.load()
  
  def load(): AlpacaConfig =
    val alpacaConfig = config.getConfig("alpaca")
    
    AlpacaConfig(
      apiKey = alpacaConfig.getString("api-key"),
      secretKey = alpacaConfig.getString("secret-key"),
      baseUrl = alpacaConfig.getString("base-url"),
      isPaper = alpacaConfig.getBoolean("paper-trading")
    )
  
  def loadFromEnv(): AlpacaConfig =
    val apiKey = sys.env.getOrElse("ALPACA_API_KEY", 
      throw new IllegalArgumentException("ALPACA_API_KEY environment variable is required"))
    val secretKey = sys.env.getOrElse("ALPACA_SECRET_KEY", 
      throw new IllegalArgumentException("ALPACA_SECRET_KEY environment variable is required"))
    val baseUrl = sys.env.getOrElse("ALPACA_BASE_URL", "https://paper-api.alpaca.markets")
    val isPaper = sys.env.getOrElse("ALPACA_PAPER_TRADING", "true").toBoolean
    
    AlpacaConfig(
      apiKey = apiKey,
      secretKey = secretKey,
      baseUrl = baseUrl,
      isPaper = isPaper
    )
