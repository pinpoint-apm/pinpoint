import 'billboard.js/dist/billboard.css';
import React from 'react';
import bb, { bar, ChartOptions, DataItem } from 'billboard.js';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { abbreviateNumber, getMaxTickValue } from '@pinpoint-fe/ui/src/utils';

export interface ResponseAvgMaxChartProps {
  /**
   * The order of the data matches the order of the colors.
   */
  data: number[] | ((categories: ResponseAvgMaxChartProps['categories']) => number[]);
  categories?: string[];
  colors?: string[];
  title?: React.ReactNode;
  className?: string;
  emptyMessage?: string;
}

export const ResponseAvgMaxChart = ({
  data = [],
  categories = ['Avg', 'Max'],
  colors = ['#97E386', '#13B6E7'],
  title,
  className,
  emptyMessage = 'No Data',
}: ResponseAvgMaxChartProps) => {
  const chartComponent = React.useRef<IChart>(null);
  const chartData = typeof data === 'function' ? data(categories) : data || [];

  React.useEffect(() => {
    const chart = chartComponent.current?.instance;

    chart?.config('axis.x.categories', categories);
    chart?.config('axis.y.max', getMaxTickValue([chartData]));
    chart?.config('data.color', getColors());
    chart?.load({ columns: getColumns() });
  }, [chartData, colors, categories]);

  const getColumns = () => {
    return [['data', ...chartData]];
  };

  const getColors = (): ((color: string, d: DataItem<number>) => string) => {
    return (defaultColor, { index }) => colors[index!] || defaultColor;
  };

  const getInitialOptions = (): ChartOptions => {
    return {
      data: {
        columns: getColumns(),
        empty: {
          label: {
            text: emptyMessage,
          },
        },
        type: bar(),
        color: getColors(),
        labels: {
          // colors: getComputedStyle(document.body).getPropertyValue('--text-primary'),
          format: (v: number) => abbreviateNumber(v, ['ms', 'sec']),
        },
      },
      padding: {
        mode: 'fit',
        top: 20,
        right: 20, // (or 25)
      },
      legend: {
        show: false,
      },
      axis: {
        rotated: true,
        x: {
          type: 'category',
          categories,
        },
        y: {
          tick: {
            count: 3,
            format: (v: number): string => abbreviateNumber(v, ['ms', 'sec']),
          },
          padding: {
            top: 0,
            bottom: 0,
          },
          min: 0,
          max: getMaxTickValue([chartData]),
          default: [0, 10],
        },
      },
      grid: {
        y: {
          show: true,
        },
      },
      tooltip: {
        show: false,
      },
      transition: {
        duration: 0,
      },
    };
  };

  return (
    <div className="w-full h-full">
      {title}
      <BillboardJS
        bb={bb}
        ref={chartComponent}
        className={className}
        options={getInitialOptions()}
      />
    </div>
  );
};
