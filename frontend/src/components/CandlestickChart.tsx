import React, { useEffect, useRef } from 'react';
import { createChart, ColorType, IChartApi, ISeriesApi, CandlestickData, Time, CandlestickSeries } from 'lightweight-charts';

interface Bar {
  t: string;
  o: number;
  h: number;
  l: number;
  c: number;
  v: number;
  n: number;
  vw: number;
}

interface CandlestickChartProps {
  data: Bar[];
  symbol: string;
  width?: number;
  height?: number;
}

const CandlestickChart: React.FC<CandlestickChartProps> = ({ 
  data, 
  symbol, 
  width = 600, 
  height = 400 
}) => {
  const chartContainerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const seriesRef = useRef<ISeriesApi<"Candlestick"> | null>(null);

  useEffect(() => {
    if (!chartContainerRef.current) return;

    // Create chart with options matching the documentation
    const chartOptions = {
      width,
      height,
      layout: {
        textColor: 'white',
        background: { type: ColorType.Solid, color: 'rgba(255, 255, 255, 0.1)' }
      },
      grid: {
        vertLines: { color: 'rgba(255, 255, 255, 0.1)' },
        horzLines: { color: 'rgba(255, 255, 255, 0.1)' },
      },
      crosshair: {
        mode: 1,
      },
      rightPriceScale: {
        borderColor: 'rgba(255, 255, 255, 0.2)',
      },
      timeScale: {
        borderColor: 'rgba(255, 255, 255, 0.2)',
        timeVisible: true,
        secondsVisible: false,
      },
    };

    const chart = createChart(chartContainerRef.current, chartOptions);
    
    // Add candlestick series using the correct API
    const candlestickSeries = chart.addSeries(CandlestickSeries, {
      upColor: '#26a69a',
      downColor: '#ef5350',
      borderVisible: false,
      wickUpColor: '#26a69a',
      wickDownColor: '#ef5350'
    });

    // Store references
    chartRef.current = chart;
    seriesRef.current = candlestickSeries;

    // Cleanup function
    return () => {
      if (chartRef.current) {
        chartRef.current.remove();
      }
    };
  }, []); // Empty dependency array - chart is created once

  useEffect(() => {
    if (!seriesRef.current || !data.length) return;

    // Convert data to TradingView format
    const chartData: CandlestickData<Time>[] = data.map(bar => ({
      time: new Date(bar.t).getTime() / 1000 as Time, // Convert to seconds timestamp
      open: bar.o,
      high: bar.h,
      low: bar.l,
      close: bar.c,
    }));

    // Set the data
    seriesRef.current.setData(chartData);

    // Fit content to view
    if (chartRef.current) {
      chartRef.current.timeScale().fitContent();
    }
  }, [data]); // Dependency: data

  if (!data.length) {
    return (
      <div style={{ 
        width, 
        height, 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'center',
        background: 'rgba(255, 255, 255, 0.05)',
        borderRadius: '8px',
        border: '1px solid rgba(255, 255, 255, 0.1)'
      }}>
        <p style={{ color: 'rgba(255, 255, 255, 0.7)' }}>No data available for {symbol}</p>
      </div>
    );
  }

  return (
    <div style={{ 
      background: 'rgba(255, 255, 255, 0.05)', 
      borderRadius: '8px', 
      padding: '10px',
      border: '1px solid rgba(255, 255, 255, 0.1)',
      width,
      height,
    }}>
      <h3 style={{ 
        margin: '0 0 10px 0', 
        color: 'white', 
        fontSize: '1.2rem',
        textAlign: 'center'
      }}>
        {symbol} Candlestick Chart
      </h3>
      <div ref={chartContainerRef} />
    </div>
  );
};

export default CandlestickChart;
