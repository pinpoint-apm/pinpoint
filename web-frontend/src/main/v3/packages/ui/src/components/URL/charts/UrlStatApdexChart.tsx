import 'billboard.js/dist/billboard.css';
import React from 'react';
import bb, { ChartOptions, line } from 'billboard.js';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { isValid } from 'date-fns';
import { UrlStatChart as UrlStatChartApi } from '@pinpoint-fe/constants';
import { cn } from '../../../lib';
import { formatNewLinedDateString, numberInDecimal } from '@pinpoint-fe/ui/utils';
import { colors } from '../../../constant/theme';

export interface UrlStatApdexChartProps {
  data: UrlStatChartApi.Response | undefined;
  className?: string;
  emptyMessage?: string;
}

const chartColors = [colors.green[400]];

export const UrlStatApdexChart = ({
  data,
  className,
  emptyMessage = 'No Data',
}: UrlStatApdexChartProps) => {
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
          text: 'Apdex Score',
          position: 'outer-middle',
        },
        // tick: {
        //   format: (v: number) => `${v}ms`,
        // },
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
        value: (v: number) => `${numberInDecimal(v, 2)}`,
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
