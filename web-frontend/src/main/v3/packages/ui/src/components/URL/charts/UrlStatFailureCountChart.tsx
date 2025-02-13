import 'billboard.js/dist/billboard.css';
import React from 'react';
import bb, { ChartOptions, bar } from 'billboard.js';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { abbreviateNumber, formatNewLinedDateString } from '@pinpoint-fe/ui/src/utils';
import { isValid } from 'date-fns';
import { UrlStatChartType as UrlStatChartApi, colors } from '@pinpoint-fe/ui/src/constants';
import { cn } from '../../../lib';

export interface UrlStatFailureCountChartProps {
  data: UrlStatChartApi.Response | undefined;
  className?: string;
  emptyMessage?: string;
}

const chartColors = [
  colors.emerald[300],
  colors.emerald[400],
  colors.emerald[500],
  colors.emerald[600],
  colors.blue[300],
  colors.orange[300],
  colors.orange[500],
  colors.red[500],
];

export const UrlStatFailureCountChart = ({
  data,
  className,
  emptyMessage = 'No Data',
}: UrlStatFailureCountChartProps) => {
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
      type: bar(),
      colors: keyList.reduce((prev, curr, i) => {
        return { ...prev, [curr]: chartColors[i] };
      }, {}),
      groups: [yData.map(([fieldName]) => fieldName as string)],
      order: null,
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
          text: 'Failure Count',
          position: 'outer-middle',
        },
        tick: {
          format: (v: number) => abbreviateNumber(v, ['', 'K', 'M', 'G']),
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
        show: true,
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
        value: (v: number) => abbreviateNumber(v, ['', 'K', 'M', 'G']),
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
