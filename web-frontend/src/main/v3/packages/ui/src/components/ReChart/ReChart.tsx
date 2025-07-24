import React from 'react';
import { formatInTimeZone } from 'date-fns-tz';
import { isThisYear, isToday } from 'date-fns';
import { XAxis, YAxis, ComposedChart, TooltipProps } from 'recharts';
import { useRechart } from './useRechart';
import { ChartContainer, ChartLegend, ChartLegendContent, ChartTooltip } from '../ui';
import { Payload } from 'recharts/types/component/DefaultLegendContent';
import { Chart } from '@pinpoint-fe/ui/src/constants';
import { getFormat, getTimezone } from '@pinpoint-fe/ui/src/utils';
import { CustomChartTooltipContent } from './ChartTooltipContent';

export interface ReChartProps {
  syncId?: string | number;
  chartData: Chart;
  unit?: string;
  tooltipConfig?: TooltipProps<number, string> & { showTotal?: boolean };
  isAnimationActive?: boolean;
  xAxisTickFormatter?: (value: number) => string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  yAxisTickFormatter?: (value: any) => string;
}

export function defaultTickFormatter(value: number) {
  const timezone = getTimezone();

  if (isToday(value)) {
    return formatInTimeZone(value, timezone, 'HH:mm:ss');
  }
  if (isThisYear(value)) {
    return `${formatInTimeZone(value, timezone, 'MM.dd')}\n${formatInTimeZone(value, timezone, 'HH:mm:ss')}`;
  }
  return `${formatInTimeZone(value, timezone, 'yyyy.MM.dd')}\n${formatInTimeZone(value, timezone, 'HH:mm:ss')}`;
}

export const ReChart = ({
  syncId,
  chartData,
  unit = '',
  tooltipConfig,
  isAnimationActive = true,
  xAxisTickFormatter,
  yAxisTickFormatter: customYAxisTickFormatter,
}: ReChartProps) => {
  const { data, chartConfig, renderChartChildComponents } = useRechart(chartData);
  const chartContainerRef = React.useRef<HTMLDivElement>(null);
  const [hoverKey, setHoverKey] = React.useState<Payload['dataKey'] | undefined>();

  const handleMouseEnter = (item: Payload) => {
    setHoverKey(item?.dataKey);
  };

  const handleMouseLeave = () => {
    setHoverKey(undefined);
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  function yAxisTickFormatter(value: any) {
    return customYAxisTickFormatter
      ? customYAxisTickFormatter(value)
      : getFormat(unit || '')(value);
  }

  return (
    <ChartContainer config={chartConfig} className="w-full h-full" ref={chartContainerRef}>
      <ComposedChart
        syncId={syncId}
        data={data}
        margin={{
          top: 5,
          right: 30,
          left: 20,
          bottom: 5,
        }}
      >
        <XAxis
          dataKey="timestamp"
          tick={(props) => <XAxisTick {...props} tickFormatter={xAxisTickFormatter} />}
        />
        <YAxis
          tickFormatter={yAxisTickFormatter}
          label={{
            value: unit,
            position: 'insideLeft',
            angle: -90,
            style: { fontSize: '0.75rem' },
          }}
        />
        <ChartTooltip
          content={
            <CustomChartTooltipContent
              formatter={yAxisTickFormatter}
              labelFormatter={defaultTickFormatter}
              showTotal={tooltipConfig?.showTotal}
              chartWidth={chartContainerRef?.current?.offsetWidth || 392}
            />
          }
        />
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
              isAnimationActive,
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
