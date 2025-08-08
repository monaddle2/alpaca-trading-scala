# Alpaca Trading API Test Script

This Scala script tests the Alpaca Trading API client with actual web requests. It performs comprehensive testing of account information, market data, orders, and positions.

## Prerequisites

1. **Alpaca Account**: You need an Alpaca trading account (paper or live)
2. **API Keys**: Get your API key and secret from the Alpaca dashboard
3. **Scala Environment**: Make sure you have Scala 3.3.1+ and sbt installed

## Setup

### 1. Set Environment Variables

Set your Alpaca API credentials as environment variables:

```bash
# Required
export ALPACA_KEY="your_api_key_here"
export ALPACA_SECRET="your_secret_key_here"

# Optional (defaults to paper trading)
export ALPACA_BASE_URL="https://paper-api.alpaca.markets"  # or live URL
export ALPACA_PAPER_TRADING="true"  # set to "false" for live trading
```

**Quick Setup with Environment File:**
```bash
# Copy the example file
cp env.example .env

# Edit with your actual credentials
nano .env

# Source the environment file
source .env

# Run the tests
./run-test.sh
```

### 2. Build the Project

```bash
sbt compile
```

## Running the Test Script

### Option 1: Use the Convenient Shell Script (Recommended)

The easiest way to run the tests is using the provided shell script:

```bash
# Make sure the script is executable
chmod +x run-test.sh

# Run the test script
./run-test.sh
```

**What `run-test.sh` does:**
- ✅ **Validates environment variables** - Checks that `ALPACA_KEY` and `ALPACA_SECRET` are set
- ✅ **Sets default values** - Automatically sets paper trading defaults if not specified
- ✅ **Shows configuration** - Displays your API key (masked) and settings
- ✅ **Builds the project** - Runs `sbt compile` to ensure everything is built
- ✅ **Runs the tests** - Executes the Scala test script with proper error handling

**Example output:**
```bash
🚀 Alpaca Trading API Test Script Runner
==========================================
✅ Environment variables configured:
   API Key: PKD4YGCS...
   Secret Key: biTtat9z...
   Base URL: https://paper-api.alpaca.markets
   Paper Trading: true

🔨 Building project...
🧪 Running Alpaca API tests...
```

### Option 2: Run with sbt directly

```bash
sbt "runMain AlpacaTestScript"
```

### Option 3: Create a JAR and run

```bash
# Create a fat JAR
sbt assembly

# Run the JAR
java -jar target/scala-3.3.1/alpaca-trading-scala-assembly-0.1.0-SNAPSHOT.jar
```

## What the Script Tests

The test script performs the following operations:

### 1. Account Information
- Retrieves account details
- Shows buying power, cash, portfolio value
- Displays account status and restrictions

### 2. Market Data
- Gets latest trades for AAPL, MSFT, GOOGL
- Retrieves latest quotes (bid/ask)
- Fetches recent price bars (OHLCV data)

### 3. Orders
- Lists recent orders (read-only)
- Shows order status and details

### 4. Positions
- Lists current positions (read-only)
- Shows unrealized P&L

### 5. Order Operations (Paper Trading Only)
- Submits a limit order for 1 AAPL share
- Cancels the order after a short delay
- **Only runs in paper trading mode for safety**

## Safety Features

- **Paper Trading Default**: The script defaults to paper trading for safety
- **Small Order Size**: Test orders use minimal quantities
- **Order Cancellation**: Test orders are automatically cancelled
- **Error Handling**: Comprehensive error handling and logging

## Expected Output

```
🚀 Starting Alpaca Trading API Test Script
==================================================
✅ Configuration loaded successfully
   Base URL: https://paper-api.alpaca.markets
   Paper Trading: true

🧪 Running Alpaca API Tests
==================================================

📊 Test 1: Account Information
------------------------------
✅ Account retrieved successfully
   Account ID: 12345678-1234-1234-1234-123456789012
   Status: ACTIVE
   Currency: USD
   Buying Power: 100000.00
   Cash: 100000.00
   Portfolio Value: 100000.00
   Pattern Day Trader: false
   Trading Blocked: false
   Created At: 2023-01-01T00:00:00Z

📈 Test 2: Market Data
----------------------
📊 Getting data for AAPL:
   Latest Trade: 150.25 at 2023-12-01T10:30:00Z
   Latest Quote: Bid 150.20 Ask 150.30
   Recent Bars: 5 bars retrieved
     2023-12-01T10:25:00Z: O:$150.10 H:$150.30 L:$150.05 C:$150.25 V:1234
     ...

📋 Test 3: Get Orders
---------------------
✅ Retrieved 5 orders
   Order abc123: AAPL buy 10 @ limit
     Status: filled, Created: 2023-11-30T15:30:00Z
   ...

💼 Test 4: Get Positions
------------------------
✅ Retrieved 2 positions
   AAPL: 100 shares @ 150.00
     Market Value: 15000.00, Unrealized P&L: 500.00
   ...

🛒 Test 5: Order Operations (Paper Trading)
------------------------------------------
📝 Submitting limit order: Buy 1 AAPL @ $150.00
✅ Order submitted successfully!
   Order ID: def456
   Status: pending_new
   Symbol: AAPL
   Side: buy
   Quantity: 1
   Type: limit
   Limit Price: 150.00
   Created At: 2023-12-01T10:35:00Z
🔄 Cancelling order def456...
✅ Order def456 cancelled successfully

✅ All tests completed!
```

## Troubleshooting

### Common Issues

1. **Authentication Errors**
   - Verify your API key and secret are correct
   - Check that your account is active
   - Make sure environment variables are named `ALPACA_KEY` and `ALPACA_SECRET`

2. **Script Permission Issues**
   ```bash
   # If you get "Permission denied" when running run-test.sh
   chmod +x run-test.sh
   ```

3. **Environment Variable Issues**
   ```bash
   # Check if variables are set
   echo $ALPACA_KEY
   echo $ALPACA_SECRET
   
   # If not set, export them
   export ALPACA_KEY="your_key_here"
   export ALPACA_SECRET="your_secret_here"
   ```

2. **Rate Limiting**
   - The script includes delays between requests
   - If you get rate limit errors, increase the sleep times

3. **Network Issues**
   - Check your internet connection
   - Verify the API base URL is correct

4. **Paper vs Live Trading**
   - Paper trading uses different endpoints
   - Make sure you're using the correct base URL

### Debug Mode

To see more detailed logging, you can modify the logback configuration in `src/main/resources/logback.xml`.

## Security Notes

- **Never commit API keys** to version control
- **Use environment variables** for sensitive data
- **Test with paper trading** before using live trading
- **Monitor your account** for any unexpected activity
- **The `run-test.sh` script masks your API keys** in the output for security

## API Rate Limits

Alpaca has rate limits:
- **Paper Trading**: 200 requests per minute
- **Live Trading**: 200 requests per minute

The script includes delays to respect these limits, but be mindful when running multiple instances.

## Files Overview

- **`run-test.sh`** - Convenient shell script to run tests with validation
- **`env.example`** - Template for environment variables
- **`AlpacaTestScript.scala`** - Main test script with comprehensive API testing
- **`TEST_SCRIPT_README.md`** - This documentation file
