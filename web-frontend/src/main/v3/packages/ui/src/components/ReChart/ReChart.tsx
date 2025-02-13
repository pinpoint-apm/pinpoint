import React from 'react';
import { format, isThisYear, isToday } from 'date-fns';
import { XAxis, YAxis, ComposedChart, TooltipProps } from 'recharts';
import { useRechart } from './useRechart';
import { ChartContainer, ChartLegend, ChartLegendContent, ChartTooltip } from '../ui';
import { Payload } from 'recharts/types/component/DefaultLegendContent';
import { Chart } from '@pinpoint-fe/ui/src/constants';
import { getFormat } from '@pinpoint-fe/ui/src/utils';
import { CustomChartTooltipContent } from './ChartTooltipContent';

export interface ReChartProps {
  syncId?: string | number;
  chartData: Chart;
  unit?: string;
  tooltipConfig?: TooltipProps<number, string> & { showTotal?: boolean };
  xAxisTickFormatter?: (value: number) => string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  yAxisTickFormatter?: (value: any) => string;
}

function defaultTickFormatter(value: number) {
  if (isToday(value)) {
    return format(value, 'HH:mm:ss');
  }
  if (isThisYear(value)) {
    return `${format(value, 'MM.dd')}\n${format(value, 'HH:mm:ss')}`;
  }
  return `${format(value, 'yyyy.MM.dd')}\n${format(value, 'HH:mm:ss')}`;
}

export const ReChart = ({
  syncId,
  chartData,
  unit = '',
  tooltipConfig,
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
