import React from 'react';
import { format, isThisYear, isToday } from 'date-fns';
import { XAxis, YAxis, ComposedChart } from 'recharts';
import { useRechart } from './useRechart';
import {
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from '../ui';
import { Payload } from 'recharts/types/component/DefaultLegendContent';
import { Chart } from '@pinpoint-fe/constants';

export interface ReChartProps {
  chartData: Chart;
  unit?: string;
}

export const ReChart = ({ chartData, unit = '' }: ReChartProps) => {
  const { data, chartConfig, renderChartChildComponents } = useRechart(chartData);
  const chartContainerRef = React.useRef<HTMLDivElement>(null);
  const [hoverKey, setHoverKey] = React.useState<Payload['dataKey'] | undefined>();

  const handleMouseEnter = (item: Payload) => {
    setHoverKey(item?.dataKey);
  };

  const handleMouseLeave = () => {
    setHoverKey(undefined);
  };

  return (
    <ChartContainer config={chartConfig} className="w-full h-full" ref={chartContainerRef}>
      <ComposedChart
        data={data}
        margin={{
          top: 5,
          right: 30,
          left: 20,
          bottom: 5,
        }}
      >
        <XAxis dataKey="timestamp" tick={XAxisTick} />
        <YAxis
          tickFormatter={(value) => `${value}${unit}`}
          label={{
            value: unit,
            position: 'insideLeft',
            angle: -90,
            style: { fontSize: '0.75rem' },
          }}
        />
        <ChartTooltip content={<ChartTooltipContent hideLabel={true} />} />
        <ChartLegend
          content={
            <ChartLegendContent
              mouseHoverDataKey={hoverKey}
              onLegendMouseOver={handleMouseEnter}
              onMouseLeave={handleMouseLeave}
            />
          }
        />
        {
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          renderChartChildComponents((config: any) => {
            return {
              strokeOpacity: !!hoverKey && hoverKey !== config?.label ? 0.3 : 1,
            };
          })
        }
      </ComposedChart>
    </ChartContainer>
  );
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const XAxisTick = (props: any) => {
  const { payload, tickFormatter, x, y } = props;

  function defaultTickFormatter(value: number) {
    if (isToday(value)) {
      return format(value, 'HH:mm');
    }
    if (isThisYear(value)) {
      return `${format(value, 'MM.dd')}\n${format(value, 'HH:mm')}`;
    }
    return `${format(value, 'yyyy.MM.dd')}\n${format(value, 'HH:mm')}`;
  }

  const tickString = tickFormatter?.(payload?.value) || defaultTickFormatter(payload?.value);
  return (
    <g transform={`translate(${x},${y})`}>
      {tickString?.split('\n').map((tString: string, index: number) => (
        <text key={index} x={0} y={index * 10} dy={10} textAnchor="middle" fill="#666">
          {tString}
        </text>
      ))}
    </g>
  );
};
