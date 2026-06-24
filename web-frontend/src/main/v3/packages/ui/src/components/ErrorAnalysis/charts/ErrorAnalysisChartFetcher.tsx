import 'billboard.js/dist/billboard.css';
import React from 'react';
import { useGetErrorAnalysisChartData } from '@pinpoint-fe/ui/src/hooks';
import bb, { ChartOptions, line, canvas, grid } from 'billboard.js/canvas';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { abbreviateNumber, formatNewLinedDateString } from '@pinpoint-fe/ui/src/utils';
import { isValid } from 'date-fns';
import { cn } from '../../../lib';

export interface ErrorAnalysisChartFetcherProps {
  className?: string;
  emptyMessage?: string;
}

export const ErrorAnalysisChartFetcher = ({
  className,
  emptyMessage = 'No Data',
}: ErrorAnalysisChartFetcherProps) => {
  const { data } = useGetErrorAnalysisChartData();
  const chartComponent = React.useRef<IChart>(null);
  const options: ChartOptions = {
    // v4 ESM: canvas 렌더링 모드 사용. grid 모듈은 더 이상 자동 번들되지 않아 명시적으로 등록한다.
    render: {
      mode: canvas(),
    },
    ...grid(),
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
      mode: 'fit',
      top: 20,
      bottom: 10,
      right: 25,
      left: 15,
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
          text: 'Error Count',
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
        value: (v: number) => abbreviateNumber(v, ['', 'K', 'M', 'G']),
      },
    },
    resize: {
      auto: 'parent',
      timer: false,
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
