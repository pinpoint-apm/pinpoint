import 'billboard.js/dist/billboard.css';
import React from 'react';
import bb, { bar, ChartOptions, DataItem } from 'billboard.js';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { abbreviateNumber, addCommas, getMaxTickValue } from '@pinpoint-fe/ui/utils';
import { HelpPopover } from '../../../components/HelpPopover';

export interface ResponseSummaryChartProps {
  /**
   * The order of the data matches the order of the colors.
   */
  data: number[] | ((categories: ResponseSummaryChartProps['categories']) => number[]);
  categories: string[];
  colors?: string[];
  title?: React.ReactNode;
  className?: string;
  emptyMessage?: string;
}

export const ResponseSummaryChart = ({
  data,
  categories = ['1s', '3s', '5s', 'Slow', 'Error'],
  colors = ['#34b994', '#51afdf', '#ffba00', '#e67f22', '#e95459'],
  className,
  title,
  emptyMessage = 'No Data',
}: ResponseSummaryChartProps) => {
  const containerRef = React.useRef<HTMLDivElement>(null);
  const chartComponent = React.useRef<IChart>(null);
  const chartData = typeof data === 'function' ? data(categories) : data || [];

  React.useEffect(() => {
    const chart = chartComponent.current?.instance;

    chart?.config('axis.x.categories', categories);
    chart?.config('axis.y.max', getMaxTickValue([chartData]));
    chart?.config('data.color', getColors());
    chart?.load({ columns: getColumns() });
  }, [chartData, categories, colors]);

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
        labels: {
          // colors: window?.getComputedStyle?.(document.body).getPropertyValue('--text-primary'),
          format: (v: number) => addCommas(v.toString()),
        },
        empty: {
          label: {
            text: emptyMessage,
          },
        },
        color: getColors(),
        // color: (_, {index}: DataItem): string => this.chartColors[index],
        type: bar(),
      },
      padding: {
        mode: 'fit',
        top: 20,
      },
      legend: {
        show: false,
      },
      axis: {
        x: {
          type: 'category',
          categories: categories,
        },
        y: {
          tick: {
            count: 3,
            format: (v: number): string => abbreviateNumber(v, ['', 'K', 'M', 'G']),
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
    };
  };

  return (
    <div className="w-full h-full" ref={containerRef}>
      <div className="flex gap-1">
        {title}
        <HelpPopover helpKey="HELP_VIEWER.RESPONSE_SUMMARY" />
      </div>
      <BillboardJS
        bb={bb}
        ref={chartComponent}
        className={className}
        options={getInitialOptions()}
      />
    </div>
  );
};
