#!/bin/bash

# Alpaca Trading API Test Script Runner
# This script helps you run the Alpaca test script with proper environment setup

set -e

echo "🚀 Alpaca Trading API Test Script Runner"
echo "=========================================="

# Check if required environment variables are set
if [ -z "$ALPACA_KEY" ]; then
    echo "❌ Error: ALPACA_KEY environment variable is not set"
    echo "Please set your Alpaca API key:"
    echo "export ALPACA_KEY=\"your_api_key_here\""
    exit 1
fi

if [ -z "$ALPACA_SECRET" ]; then
    echo "❌ Error: ALPACA_SECRET environment variable is not set"
    echo "Please set your Alpaca secret key:"
    echo "export ALPACA_SECRET=\"your_secret_key_here\""
    exit 1
fi

# Set default values for optional environment variables
export ALPACA_BASE_URL=${ALPACA_BASE_URL:-"https://paper-api.alpaca.markets"}
export ALPACA_PAPER_TRADING=${ALPACA_PAPER_TRADING:-"true"}

echo "✅ Environment variables configured:"
echo "   API Key: ${ALPACA_KEY:0:8}..."
echo "   Secret Key: ${ALPACA_SECRET:0:8}..."
echo "   Base URL: $ALPACA_BASE_URL"
echo "   Paper Trading: $ALPACA_PAPER_TRADING"
echo ""

# Check if sbt is available
if ! command -v sbt &> /dev/null; then
    echo "❌ Error: sbt is not installed or not in PATH"
    echo "Please install sbt first: https://www.scala-sbt.org/download.html"
    exit 1
fi

echo "🔨 Building project..."
sbt compile

echo ""
echo "🧪 Running Alpaca API tests..."
echo ""

# Run the test script
sbt "runMain AlpacaTestScript"

echo ""
echo "✅ Test script completed!"
