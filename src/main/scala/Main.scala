package com.example.alpaca

import com.example.alpaca.config.Config
import com.example.alpaca.trading.AlpacaTradingClient
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.ExecutionContext.Implicits.global

@main def main(): Unit =
  val app = new AlpacaTradingApp()
  app.run()

class AlpacaTradingApp extends LazyLogging:
  def run(): Unit =
    logger.info("Starting Alpaca Trading Application")
    
    try
      val config = Config.load()
      val client = new AlpacaTradingClient(config)
      
      // Example: Get account information
      val accountFuture = client.getAccount()
      accountFuture.foreach { account =>
        logger.info(s"Account status: ${account.status}")
        logger.info(s"Buying power: ${account.buyingPower}")
      }
      
      // Example: Get market data for a symbol
      val barsFuture = client.getBars("AAPL", "1Day", limit = Some(5))
      barsFuture.foreach { bars =>
        logger.info(s"Retrieved ${bars.length} bars for AAPL")
      }
      
    catch
      case e: Exception =>
        logger.error("Error running trading application", e)
        throw e
