import 'billboard.js/dist/billboard.css';
import React from 'react';
import bb, { ChartOptions, line } from 'billboard.js';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { addCommas, formatNewLinedDateString, numberInInteger } from '@pinpoint-fe/ui/utils';
import { isValid } from 'date-fns';
import { UrlStatChartType as UrlStatChartApi, colors } from '@pinpoint-fe/ui/constants';
import { cn } from '../../../lib';

export interface UrlStatLatencyChartProps {
  data: UrlStatChartApi.Response | undefined;
  className?: string;
  emptyMessage?: string;
}

const chartColors = [colors.violet[800], colors.blue[600]];

export const UrlStatLatencyChart = ({
  data,
  className,
  emptyMessage = 'No Data',
}: UrlStatLatencyChartProps) => {
  const chartComponent = React.useRef<IChart>(null);
  const yData = data
    ? data.metricValueGroups[0].metricValues.map(({ fieldName, values }) => {
        return [fieldName, ...values.map((v: number) => (v < 0 ? null : v))];
      })
    : [];
  const keyList = yData.map(([fieldName]) => fieldName as string);
  const options: ChartOptions = {
    data: {
      x: 'dates',
      columns: [],
      empty: {
        label: {
          text: emptyMessage,
        },
      },
      type: line(),
      colors: keyList.reduce((prev, curr, i) => {
        return { ...prev, [curr]: chartColors[i] };
      }, {}),
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
              return `${formatNewLinedDateString(date)}`;
            }
            return '';
          },
        },
      },
      y: {
        label: {
          text: 'Latency (ms)',
          position: 'outer-middle',
        },
        tick: {
          format: (v: number) => `${addCommas(numberInInteger(v))}ms`,
        },
        padding: {
          bottom: 0,
        },
        min: 0,
        default: [0, 10],
      },
    },
    grid: {
      y: {
        show: false,
      },
    },
    point: {
      show: false,
    },
    transition: {
      duration: 0,
    },
    tooltip: {
      order: '',
      format: {
        value: (v: number) => `${addCommas(numberInInteger(v))}ms`,
      },
    },
  };

  React.useEffect(() => {
    const chart = chartComponent.current?.instance;

    chart?.load({
      columns: data
        ? [
            ['dates', ...data.timestamp],
            ...data.metricValueGroups[0].metricValues.map(({ fieldName, values }) => {
              return [fieldName, ...values.map((v: number) => (v < 0 ? null : v))];
            }),
          ]
        : [],
      resizeAfter: true,
    });
  }, [data]);

  return (
    <BillboardJS
      bb={bb}
      ref={chartComponent}
      className={cn('w-full h-full', className)}
      options={options}
    />
  );
};
