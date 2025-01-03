import React from 'react';
import bb, { ChartOptions } from 'billboard.js';
import { isValid } from 'date-fns';
import deepmerge from 'deepmerge';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { cn, DEFAULT_CHART_CONFIG } from '../../../lib';
import { InspectorAgentChart, InspectorApplicationChart } from '@pinpoint-fe/ui/constants';
import { formatNewLinedDateString } from '@pinpoint-fe/ui/utils';

export interface ChartCoreProps {
  data: InspectorAgentChart.Response | InspectorApplicationChart.Response;
  chartOptions?: ChartOptions;
  className?: string;
  emptyMessage?: string;
  style?: React.CSSProperties;
}

export const ChartCore = ({
  data,
  chartOptions = {},
  className,
  emptyMessage = 'No Data',
  style,
}: ChartCoreProps) => {
  const prevData = React.useRef([] as (string | number | null)[][]);
  const chartComponent = React.useRef<IChart>(null);
  const defaultOptions = {
    data: {
      x: 'dates',
      columns: [],
      empty: {
        label: {
          text: emptyMessage,
        },
      },
    },
    padding: {
      mode: 'fit',
      top: DEFAULT_CHART_CONFIG.PADDING_TOP,
      bottom: DEFAULT_CHART_CONFIG.PADDING_BOTTOM,
      right: DEFAULT_CHART_CONFIG.PADDING_RIGHT,
      left: DEFAULT_CHART_CONFIG.PADDING_LEFT,
    },
    axis: {
      x: {
        type: 'timeseries',
        tick: {
          count: DEFAULT_CHART_CONFIG.X_AXIS_TICK_COUNT,
          format: (date: Date) => {
            if (isValid(date)) {
              return `${formatNewLinedDateString(date)}`;
            }
            return '';
          },
        },
      },
      y: {
        padding: {
          bottom: 0,
        },
        min: 0,
        default: [0, DEFAULT_CHART_CONFIG.DEFAULT_MAX],
      },
    },
    point: {
      r: 0,
      focus: {
        only: true,
        expand: {
          r: 3,
        },
      },
    },
    tooltip: {
      linked: true,
      order: '',
    },
    resize: {
      auto: false,
    },
  };
  const options = deepmerge(defaultOptions, chartOptions);

  React.useEffect(() => {
    window.addEventListener('resize', () => {
      const chart = chartComponent.current?.instance;
      chart?.resize();
    });
  }, []);

  React.useEffect(() => {
    const chart = chartComponent.current?.instance;
    const chartData = data
      ? [
          ['dates', ...data.timestamp],
          ...data.metricValues.map(({ fieldName, valueList }) => {
            return [fieldName, ...valueList.map((v) => (v < 0 ? null : v))];
          }),
        ]
      : [];
    const maxData = Math.max(
      ...(chartData
        .slice(1)
        .map((d) => d.slice(1).filter((v) => v !== null))
        .flat() as number[]),
    );

    const prevKeys = prevData.current.slice(1).map(([fieldName]) => fieldName as string);
    const currKeys = chartData.slice(1).map(([fieldName]) => fieldName as string);
    const removedKeys = prevKeys.filter((key) => !currKeys.includes(key));
    const unload = prevKeys.length === 0 ? false : removedKeys.length !== 0;
    chart?.load({
      columns: chartData,
      unload,
    });
    chart?.axis.max(maxData === 0 ? DEFAULT_CHART_CONFIG.DEFAULT_MAX : false);
    chart?.config('tooltip.contents', chartOptions.tooltip?.contents);
    prevData.current = chartData;
  }, [data]);

  return (
    <BillboardJS
      bb={bb}
      ref={chartComponent}
      style={style}
      className={cn('w-full h-full min-h-0 overflow-hidden', className)}
      options={options}
    />
  );
};
