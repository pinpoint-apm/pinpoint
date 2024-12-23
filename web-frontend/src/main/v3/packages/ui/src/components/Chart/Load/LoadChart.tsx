import 'billboard.js/dist/billboard.css';
import React from 'react';
import bb, { areaStep, ChartOptions } from 'billboard.js';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { abbreviateNumber, addCommas, getMaxTickValue } from '@pinpoint-fe/ui/utils';
import { format, isValid } from 'date-fns';
import { cn } from '../../../lib/utils';
import { colors } from '../../../constant/theme';

export type LoadChartDataType = {
  dates?: number[];
  [key: string]: number[] | undefined;
};

export interface LoadChartProps {
  /**
   * The key value of datas and the key value of color match each other.
   */
  datas: LoadChartDataType | ((colors: LoadChartProps['chartColors']) => LoadChartDataType);
  chartColors?: {
    [key: string]: string;
  };
  title?: React.ReactNode;
  className?: string;
  emptyMessage?: string;
}

export const LoadChart = ({
  datas = {
    dates: [],
  },
  chartColors = {
    '1s': colors.fast,
    '100ms': colors.fast,
    '3s': colors.normal,
    '300ms': colors.normal,
    '5s': colors.delay,
    '500ms': colors.delay,
    Slow: colors.slow,
    Error: colors.error,
  },
  title,
  className,
  emptyMessage = 'No Data',
}: LoadChartProps) => {
  const chartData = typeof datas === 'function' ? datas?.(chartColors) : datas;
  const chartComponent = React.useRef<IChart>(null);
  const prevData = React.useRef<LoadChartDataType>({} as LoadChartDataType);

  React.useEffect(() => {
    const chart = chartComponent.current?.instance;
    const newColumns = getColumns();
    const prevKeys = Object.keys(prevData.current).slice(1);
    const currKeys = Object.keys(chartData).slice(1);
    const removedKeys = prevKeys.filter((key: string) => !currKeys.includes(key));
    const unload = prevKeys.length === 0 ? false : removedKeys.length !== 0;

    chart?.config('data.groups', getGroupsOption());
    chart?.config('axis.y.max', getMaxTickValue(getColumns() as number[][], 1));
    chart?.load({ columns: newColumns, colors: chartColors, unload });

    prevData.current = chartData;
  }, [datas, chartColors]);

  const getColumns = () => {
    const keys = Object.keys(chartData);
    return keys.map((key) => [key, ...(chartData[key] || [])]);
  };

  const getGroupsOption = () => {
    return [Object.keys(chartData).filter((key) => key !== 'dates')];
  };

  const getInitialOptions = (): ChartOptions => {
    return {
      data: {
        color: (defaultColor, { id }) => {
          return chartColors?.[id] ? chartColors?.[id] : defaultColor;
        },
        columns: getColumns(),
        empty: {
          label: {
            text: emptyMessage,
          },
        },
        groups: getGroupsOption(),
        order: null,
        type: areaStep(),
        x: 'dates',
      },
      padding: {
        mode: 'fit',
        top: 20,
        bottom: 10,
        right: 25,
      },
      axis: {
        x: {
          type: 'timeseries',
          tick: {
            count: 6,
            show: false,
            format: (date: Date) => {
              if (isValid(date)) {
                return `${format(date, 'MM.dd')}\n${format(date, 'HH:mm')}`;
              }
              return '';
            },
          },
          padding: {
            left: 0,
            right: 0,
          },
        },
        y: {
          tick: {
            count: 3,
            format: (v: number) => abbreviateNumber(v, ['', 'K', 'M', 'G']),
          },
          padding: {
            top: 0,
            bottom: 0,
          },
          min: 0,
          max: getMaxTickValue(getColumns() as number[][], 1),
          default: [0, 10],
        },
      },
      grid: {
        y: {
          show: true,
        },
      },
      point: {
        show: false,
      },
      tooltip: {
        order: '',
        format: {
          value: (v: number) => addCommas(v.toString()),
        },
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
        className={cn(`[&_.bb-line]:stroke-0`, className)}
        bb={bb}
        ref={chartComponent}
        options={getInitialOptions()}
      />
    </div>
  );
};
