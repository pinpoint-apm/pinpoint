import 'billboard.js/dist/billboard.css';
import React from 'react';
import bb, { ChartOptions, line } from 'billboard.js';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import BillboardJS, { IChart } from '@billboard.js/react';
import { cn } from '../../../lib';

export interface UrlStatDefaultChartProps {
  className?: string;
  emptyMessage?: string;
}

export const UrlStatDefaultChart = ({
  className,
  emptyMessage = 'No Data',
}: UrlStatDefaultChartProps) => {
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
      y: {
        tick: {
          count: 2,
        },
        padding: {
          top: 0,
          bottom: 0,
        },
        min: 0,
        default: [0, 1],
      },
    },
    transition: {
      duration: 0,
    },
  };

  return (
    <BillboardJS
      bb={bb}
      ref={chartComponent}
      className={cn('w-full h-full', className)}
      options={options}
    />
  );
};
