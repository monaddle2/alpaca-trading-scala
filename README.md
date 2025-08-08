# Alpaca Trading Scala

A Scala 3 project for algorithmic trading using the Alpaca Markets API.

## Features

- **Account Management**: Get account information, buying power, and portfolio value
- **Order Management**: Submit, cancel, and track orders
- **Position Management**: Get current positions and close positions
- **Market Data**: Retrieve bars (OHLCV), trades, and quotes
- **Paper Trading**: Safe testing environment with paper trading support
- **Async Operations**: All API calls are asynchronous using Scala Futures

## Prerequisites

- Scala 3.3.1 or later
- sbt (Scala Build Tool)
- Alpaca Markets account (free for paper trading)

## Setup

1. **Clone or create the project**:
   ```bash
   cd alpaca-trading-scala
   ```

2. **Configure your Alpaca API credentials**:
   
   The project is configured to use environment variables. Set these in your `~/.zshrc`:
   ```bash
   export ALPACA_KEY="your_api_key"
   export ALPACA_SECRET="your_secret_key"
   export ALPACA_BASE_URL="https://paper-api.alpaca.markets"  # Optional, defaults to paper trading
   ```

3. **Get your Alpaca API credentials**:
   - Sign up at [Alpaca Markets](https://alpaca.markets/)
   - Go to your dashboard
   - Generate API keys for paper trading
   - Copy the API Key ID and Secret Key

## Building and Running

1. **Compile the project**:
   ```bash
   sbt compile
   ```

2. **Run the application**:
   ```bash
   sbt run
   ```

3. **Run tests**:
   ```bash
   sbt test
   ```

## Usage Examples

### Basic Account Information
```scala
import com.example.alpaca.config.Config
import com.example.alpaca.trading.AlpacaTradingClient
import scala.concurrent.ExecutionContext.Implicits.global

val config = Config.loadFromEnv()
val client = new AlpacaTradingClient(config)

// Get account information
val account = client.getAccount()
account.foreach { acc =>
  println(s"Account Status: ${acc.status}")
  println(s"Buying Power: ${acc.buyingPower}")
  println(s"Portfolio Value: ${acc.portfolioValue}")
}
```

### Submit an Order
```scala
// Submit a market order to buy 10 shares of AAPL
val order = client.submitOrder(
  symbol = "AAPL",
  qty = "10",
  side = "buy",
  orderType = "market"
)

order.foreach { ord =>
  println(s"Order submitted: ${ord.id}")
  println(s"Status: ${ord.status}")
}
```

### Get Market Data
```scala
// Get the last 5 daily bars for AAPL
val bars = client.getBars("AAPL", "1Day", limit = Some(5))

bars.foreach { barList =>
  barList.foreach { bar =>
    println(s"Date: ${bar.t}, Close: ${bar.c}")
  }
}
```

### Get Current Positions
```scala
// Get all current positions
val positions = client.getPositions()

positions.foreach { posList =>
  posList.foreach { pos =>
    println(s"${pos.symbol}: ${pos.qty} shares, P&L: ${pos.unrealizedPl}")
  }
}
```

## Project Structure

```
alpaca-trading-scala/
├── build.sbt                    # Build configuration
├── src/
│   ├── main/
│   │   ├── scala/
│   │   │   └── com/example/alpaca/
│   │   │       ├── Main.scala              # Application entry point
│   │   │       ├── config/
│   │   │       │   └── Config.scala        # Configuration management
│   │   │       ├── models/
│   │   │       │   └── Models.scala        # Data models
│   │   │       └── trading/
│   │   │           └── AlpacaTradingClient.scala  # Main trading client
│   │   └── resources/
│   │       ├── application.conf             # Application configuration
│   │       └── logback.xml                 # Logging configuration
│   └── test/
│       └── scala/                          # Test files
└── README.md
```

## API Endpoints Supported

### Account
- `GET /v2/account` - Get account information

### Orders
- `GET /v2/orders` - Get all orders
- `POST /v2/orders` - Submit new order
- `DELETE /v2/orders/{order_id}` - Cancel order

### Positions
- `GET /v2/positions` - Get all positions
- `GET /v2/positions/{symbol}` - Get position for specific symbol
- `DELETE /v2/positions/{symbol}` - Close position

### Market Data
- `GET /v2/stocks/{symbol}/bars` - Get historical bars
- `GET /v2/stocks/{symbol}/trades/latest` - Get latest trade
- `GET /v2/stocks/{symbol}/quotes/latest` - Get latest quote

## Dependencies

- **sttp-client3**: HTTP client for API communication
- **circe**: JSON parsing and encoding
- **scala-logging**: Structured logging
- **typesafe-config**: Configuration management
- **scalatest**: Testing framework

## Safety Notes

⚠️ **Important**: This project defaults to paper trading for safety. When switching to live trading:

1. Change `paper-trading = false` in `application.conf`
2. Update `base-url` to `https://api.alpaca.markets`
3. Use live trading API credentials
4. Test thoroughly with small amounts first

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License.
