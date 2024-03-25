import 'billboard.js/dist/billboard.css';
import React from 'react';
import bb, { ChartOptions, line } from 'billboard.js';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { getEllipsisText } from '@pinpoint-fe/utils';
import { format, isValid } from 'date-fns';
import { SqlStatChart as SqlStatChartApi } from '@pinpoint-fe/constants';
import { cn } from '../../../lib';

export interface SqlStatMaxTimeChartProps {
  data: SqlStatChartApi.Response | undefined;
  className?: string;
  emptyMessage?: string;
}

const maxTextLength = 100;

export const SqlStatMaxTimeChart = ({
  data,
  className,
  emptyMessage = 'No Data',
}: SqlStatMaxTimeChartProps) => {
  const chartComponent = React.useRef<IChart>(null);
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
              return `${format(date, 'MM.dd')}\n${format(date, 'HH:mm')}`;
            }
            return '';
          },
        },
      },
      y: {
        label: {
          text: 'Max Time (ms)',
          position: 'outer-middle',
        },
        tick: {
          format: (v: number) => `${v}ms`,
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
        name: (name: string) => getEllipsisText({ text: name, maxTextLength }),
        value: (v: number) => `${Math.floor(v)}ms`,
      },
    },
  };

  React.useEffect(() => {
    const chart = chartComponent.current?.instance;
    const yData = data
      ? data.metricValueGroups[0].metricValues.map(({ fieldName, values }) => {
          return [fieldName, ...values.map((v: number) => (v < 0 ? null : v))];
        })
      : [];
    const keys = yData.map(([fieldName]) => fieldName as string);

    keys.forEach((key: string) => {
      chart?.data.names({
        [key]: getEllipsisText({ text: key, maxTextLength }),
      });
    });

    chart?.load({
      columns: data ? [['dates', ...data.timestamp], ...yData] : [],
      unload: true,
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
