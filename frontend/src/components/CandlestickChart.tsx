import React, { useRef, useEffect } from 'react';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  TimeScale,
} from 'chart.js';
import { Chart } from 'react-chartjs-2';
import 'chartjs-adapter-date-fns';

// Register Chart.js components
ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  Title,
  Tooltip,
  Legend,
  TimeScale
);

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
  const chartRef = useRef<ChartJS>(null);

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

  // Create candlestick data
  const chartData = {
    labels: data.map(bar => new Date(bar.t)),
    datasets: [
      {
        label: 'High',
        data: data.map(bar => bar.h),
        borderColor: 'transparent',
        backgroundColor: 'transparent',
        type: 'line' as const,
        pointRadius: 0,
        borderWidth: 0,
      },
      {
        label: 'Low',
        data: data.map(bar => bar.l),
        borderColor: 'transparent',
        backgroundColor: 'transparent',
        type: 'line' as const,
        pointRadius: 0,
        borderWidth: 0,
      },
      {
        label: 'Open',
        data: data.map(bar => bar.o),
        borderColor: 'transparent',
        backgroundColor: 'transparent',
        type: 'line' as const,
        pointRadius: 0,
        borderWidth: 0,
      },
      {
        label: 'Close',
        data: data.map(bar => bar.c),
        borderColor: 'transparent',
        backgroundColor: 'transparent',
        type: 'line' as const,
        pointRadius: 0,
        borderWidth: 0,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      title: {
        display: true,
        text: `${symbol} Candlestick Chart`,
        color: 'white',
        font: {
          size: 16,
        },
      },
      tooltip: {
        enabled: false, // Disable tooltip completely
      },
    },
    scales: {
      x: {
        type: 'time' as const,
        time: {
          unit: 'minute' as const,
          displayFormats: {
            minute: 'HH:mm',
          },
        },
        ticks: {
          color: 'white',
        },
        grid: {
          color: 'rgba(255, 255, 255, 0.1)',
        },
      },
      y: {
        ticks: {
          color: 'white',
          callback: (value: any) => `$${value.toFixed(2)}`,
        },
        grid: {
          color: 'rgba(255, 255, 255, 0.1)',
        },
      },
    },
    interaction: {
      mode: 'nearest' as const,
      axis: 'x' as const,
      intersect: false,
    },
  };

  // Custom plugin to draw candlesticks
  const candlestickPlugin = {
    id: 'candlestick',
    afterDraw: (chart: any) => {
      const ctx = chart.ctx;
      const meta = chart.getDatasetMeta(0);
      
      data.forEach((bar, index) => {
        const isUp = bar.c >= bar.o;
        const color = isUp ? '#26a69a' : '#ef5350';
        
        // Get pixel coordinates
        const x = meta.data[index].x;
        const openY = chart.scales.y.getPixelForValue(bar.o);
        const closeY = chart.scales.y.getPixelForValue(bar.c);
        const highY = chart.scales.y.getPixelForValue(bar.h);
        const lowY = chart.scales.y.getPixelForValue(bar.l);
        
        // Draw wick
        ctx.strokeStyle = color;
        ctx.lineWidth = 1;
        ctx.beginPath();
        ctx.moveTo(x, highY);
        ctx.lineTo(x, lowY);
        ctx.stroke();
        
        // Draw body
        const bodyWidth = 6;
        const bodyHeight = Math.abs(closeY - openY);
        const bodyY = Math.min(openY, closeY);
        
        ctx.fillStyle = color;
        ctx.fillRect(x - bodyWidth/2, bodyY, bodyWidth, bodyHeight);
        
        // Draw border for body
        ctx.strokeStyle = color;
        ctx.lineWidth = 1;
        ctx.strokeRect(x - bodyWidth/2, bodyY, bodyWidth, bodyHeight);
      });
    }
  };

  return (
    <div style={{ 
      background: 'rgba(255, 255, 255, 0.05)', 
      borderRadius: '8px', 
      padding: '10px',
      border: '1px solid rgba(255, 255, 255, 0.1)',
      width,
      height,
    }}>
      <Chart 
        ref={chartRef}
        type="line"
        data={chartData} 
        options={options}
        plugins={[candlestickPlugin]}
      />
    </div>
  );
};

export default CandlestickChart;
