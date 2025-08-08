import React, { useState, useEffect } from 'react';
import './App.css';

interface AccountData {
  id: string;
  accountNumber: string;
  status: string;
  currency: string;
  buyingPower: string;
  cash: string;
  portfolioValue: string;
  patternDayTrader: boolean;
  tradingBlocked: boolean;
  createdAt: string;
}

interface MarketData {
  symbol: string;
  latestTrade: {
    t: string;
    x: string;
    p: number;
    s: number;
    c: string[];
    i: number;
    z: string;
  };
  latestQuote: {
    t: string;
    ax: string;
    ap: number;
    as: number;
    bx: string;
    bp: number;
    bs: number;
    c: string[];
    z: string;
  };
  recentBars: Array<{
    t: string;
    o: number;
    h: number;
    l: number;
    c: number;
    v: number;
    n: number;
    vw: number;
  }>;
}

function App() {
  const [accountData, setAccountData] = useState<AccountData | null>(null);
  const [marketData, setMarketData] = useState<MarketData | null>(null);
  const [symbol, setSymbol] = useState('AAPL');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchAccountData = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await fetch('/api/alpaca/account');
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      setAccountData(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch account data');
    } finally {
      setLoading(false);
    }
  };

  const fetchMarketData = async (sym: string) => {
    try {
      setLoading(true);
      setError(null);
      const response = await fetch(`/api/alpaca/market-data/${sym}`);
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      setMarketData(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch market data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAccountData();
    fetchMarketData(symbol);
  }, []);

  const handleSymbolChange = (newSymbol: string) => {
    setSymbol(newSymbol);
    fetchMarketData(newSymbol);
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>Alpaca Trading Dashboard</h1>
      </header>
      
      <main className="App-main">
        {error && (
          <div className="error-message">
            Error: {error}
          </div>
        )}

        <div className="dashboard-grid">
          {/* Account Information */}
          <div className="card">
            <h2>Account Information</h2>
            {loading ? (
              <p>Loading account data...</p>
            ) : accountData ? (
              <div className="account-info">
                <p><strong>Account ID:</strong> {accountData.id}</p>
                <p><strong>Account Number:</strong> {accountData.accountNumber}</p>
                <p><strong>Status:</strong> <span className={`status ${accountData.status.toLowerCase()}`}>{accountData.status}</span></p>
                <p><strong>Currency:</strong> {accountData.currency}</p>
                <p><strong>Buying Power:</strong> ${parseFloat(accountData.buyingPower).toLocaleString()}</p>
                <p><strong>Cash:</strong> ${parseFloat(accountData.cash).toLocaleString()}</p>
                <p><strong>Portfolio Value:</strong> ${parseFloat(accountData.portfolioValue).toLocaleString()}</p>
                <p><strong>Pattern Day Trader:</strong> {accountData.patternDayTrader ? 'Yes' : 'No'}</p>
                <p><strong>Trading Blocked:</strong> {accountData.tradingBlocked ? 'Yes' : 'No'}</p>
              </div>
            ) : (
              <p>No account data available</p>
            )}
          </div>

          {/* Market Data */}
          <div className="card">
            <h2>Market Data</h2>
            <div className="symbol-input">
              <input
                type="text"
                value={symbol}
                onChange={(e) => setSymbol(e.target.value.toUpperCase())}
                placeholder="Enter symbol (e.g., AAPL)"
              />
              <button onClick={() => fetchMarketData(symbol)} disabled={loading}>
                {loading ? 'Loading...' : 'Fetch Data'}
              </button>
            </div>
            
            {marketData && (
              <div className="market-data">
                <h3>{marketData.symbol}</h3>
                
                {/* Latest Trade */}
                <div className="trade-info">
                  <h4>Latest Trade</h4>
                  <p><strong>Price:</strong> ${marketData.latestTrade.p}</p>
                  <p><strong>Size:</strong> {marketData.latestTrade.s}</p>
                  <p><strong>Exchange:</strong> {marketData.latestTrade.x}</p>
                  <p><strong>Time:</strong> {new Date(marketData.latestTrade.t).toLocaleString()}</p>
                </div>

                {/* Latest Quote */}
                <div className="quote-info">
                  <h4>Latest Quote</h4>
                  <p><strong>Bid:</strong> ${marketData.latestQuote.bp} ({marketData.latestQuote.bs} shares)</p>
                  <p><strong>Ask:</strong> ${marketData.latestQuote.ap} ({marketData.latestQuote.as} shares)</p>
                  <p><strong>Spread:</strong> ${(marketData.latestQuote.ap - marketData.latestQuote.bp).toFixed(2)}</p>
                  <p><strong>Time:</strong> {new Date(marketData.latestQuote.t).toLocaleString()}</p>
                </div>

                {/* Recent Bars */}
                <div className="bars-info">
                  <h4>Recent 1-Minute Bars</h4>
                  <div className="bars-grid">
                    {marketData.recentBars.map((bar, index) => (
                      <div key={index} className="bar-item">
                        <p><strong>Time:</strong> {new Date(bar.t).toLocaleTimeString()}</p>
                        <p><strong>O:</strong> ${bar.o} <strong>H:</strong> ${bar.h} <strong>L:</strong> ${bar.l} <strong>C:</strong> ${bar.c}</p>
                        <p><strong>Volume:</strong> {bar.v.toLocaleString()}</p>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

export default App;
