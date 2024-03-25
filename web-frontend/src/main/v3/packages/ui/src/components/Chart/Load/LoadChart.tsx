import 'billboard.js/dist/billboard.css';
import React from 'react';
import bb, { areaStep, ChartOptions } from 'billboard.js';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { abbreviateNumber, addCommas, getMaxTickValue } from '@pinpoint-fe/utils';
import { format, isValid } from 'date-fns';

export type LoadChartDataType = {
  dates?: number[];
  [key: string]: number[] | undefined;
};

export interface LoadChartProps {
  /**
   * The key value of datas and the key value of color match each other.
   */
  datas: LoadChartDataType | ((colors: LoadChartProps['colors']) => LoadChartDataType);
  colors?: {
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
  colors = {
    '1s': '#34b994',
    '3s': '#51afdf',
    '5s': '#ffba00',
    Slow: '#e67f22',
    Error: '#e95459',
  },
  title,
  className,
  emptyMessage = 'No Data',
}: LoadChartProps) => {
  const chartData = typeof datas === 'function' ? datas?.(colors) : datas;
  const chartComponent = React.useRef<IChart>(null);

  React.useEffect(() => {
    const chart = chartComponent.current?.instance;
    const newColumns = getColumns();

    chart?.config('data.color', getColorOption());
    chart?.config('data.groups', getGroupsOption());
    chart?.config('axis.y.max', getMaxTickValue(getColumns() as number[][], 1));
    chart?.load({ columns: newColumns });
  }, [datas, colors]);

  const getColumns = () => {
    const keys = Object.keys(chartData);
    return keys.map((key) => [key, ...(chartData[key] || [])]);
  };

  const getColorOption = () => {
    return (defaultColor: string, { id }: { id: string }) => {
      return colors?.[id] ? colors?.[id] : defaultColor;
    };
  };

  const getGroupsOption = () => {
    return [Object.keys(datas).filter((key) => key !== 'dates')];
  };

  const getInitialOptions = (): ChartOptions => {
    return {
      data: {
        color: (defaultColor, { id }) => {
          return colors?.[id] ? colors?.[id] : defaultColor;
        },
        columns: getColumns(),
        empty: {
          label: {
            text: emptyMessage,
          },
        },
        groups: getGroupsOption(),
        type: areaStep(),
        x: 'dates',
      },
      padding: {
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
                return `${format(date, 'MM.dd')}\n${format(date, 'mm:ss')}`;
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
    };
  };

  return (
    <div className="w-full h-full">
      {title}
      <BillboardJS
        className={className}
        bb={bb}
        ref={chartComponent}
        options={getInitialOptions()}
      />
    </div>
  );
};
