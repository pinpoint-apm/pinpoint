import React from 'react';
import {
  ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from '../ui';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ReferenceLine,
  ResponsiveContainer,
  XAxis,
  YAxis,
} from 'recharts';
import { AgentActiveSettingType } from './AgentActiveSetting';
import { AgentActiveData } from './AgentActiveTable';

export const DefaultValue = { yMax: 100 };

export interface AgentActiveChartProps {
  loading?: boolean;
  data?: AgentActiveData[];
  setting?: AgentActiveSettingType;
  clickedActiveThread?: string;
  setClickedActiveThread?: React.Dispatch<React.SetStateAction<string>>;
}

const TICK_COUNT = 4;
const DefaultReferenceLineLength = 50;
const chartConfig = {
  '1s': {
    label: '1s',
    color: '#34b994',
  },
  '3s': {
    label: '3s',
    color: '#51afdf',
  },
  '5s': {
    label: '5s',
    color: '#ffba00',
  },
  slow: {
    label: 'slow',
    color: '#e67f22',
  },
} satisfies ChartConfig;

export const AgentActiveChart = ({
  loading,
  data,
  setting,
  clickedActiveThread,
  setClickedActiveThread,
}: AgentActiveChartProps) => {
  const chartContainerRef = React.useRef<HTMLDivElement>(null);
  const height = chartContainerRef?.current?.clientHeight || 0;
  const yMax = setting?.yMax || DefaultValue.yMax;
  const ReferenceLineLength = yMax < DefaultReferenceLineLength ? yMax : DefaultReferenceLineLength;

  return (
    <ResponsiveContainer>
      <ChartContainer
        config={chartConfig}
        className="p-1.5 z-0 min-w-[50%]"
        ref={chartContainerRef}
      >
        <BarChart
          accessibilityLayer
          data={data || []}
          onClick={(props) =>
            setClickedActiveThread?.((prev) => {
              const activeLabel = props?.activeLabel || '';
              return prev === activeLabel ? '' : activeLabel;
            })
          }
        >
          <CartesianGrid vertical={false} />
          <XAxis
            dataKey="server"
            hide={true}
            tickLine={false}
            axisLine={false}
            type="category"
            interval={0}
            tick={(props) => <CustomTick {...props} />}
          />
          <YAxis
            tickLine={false}
            axisLine={false}
            tickMargin={10}
            ticks={Array.from({ length: TICK_COUNT + 1 }, (_, index) =>
              Math.ceil((yMax / TICK_COUNT) * index),
            )}
            allowDecimals={false}
            domain={() => [0, yMax]}
          />
          <ChartTooltip
            content={
              <ChartTooltipContent
                isReverse={true}
                valueFormatter={(value) => ((value as number) < 0 ? '-' : value)}
              />
            }
          />
          <ChartLegend content={<ChartLegendContent isReverse={true} />} />
          {Object.keys(chartConfig)
            .reverse()
            .map((key) => (
              <Bar
                key={key}
                dataKey={key}
                stackId="agentActiveThread"
                fill={chartConfig[key as keyof typeof chartConfig].color}
                hide={loading}
              >
                {data?.map((entry, index) => (
                  <Cell
                    key={`cell-${index}`}
                    opacity={
                      clickedActiveThread ? (entry.server === clickedActiveThread ? 1 : 0.3) : 1
                    }
                  />
                ))}
              </Bar>
            ))}
          {Array.from({ length: ReferenceLineLength }, (_, index) => {
            return (
              <ReferenceLine
                key={index}
                y={Math.floor(yMax / ReferenceLineLength) * (index + 1)}
                stroke="white"
                strokeWidth={height >= 650 ? 4 : height >= 450 ? 3 : height >= 300 ? 2 : 1}
                isFront={true}
              />
            );
          })}
        </BarChart>
      </ChartContainer>
    </ResponsiveContainer>
  );
};

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const CustomTick = (props: any) => {
  const { payload, x, y } = props;
  const maxLength = Math.ceil(payload?.offset / 10);
  const value = payload?.value;
  const displayValue = value?.length <= maxLength ? value : value?.slice(0, maxLength) + '...';

  return (
    <g transform={`translate(${x},${y})`}>
      <text x={0} y={0} dy={16} textAnchor="middle" fill="#666">
        {displayValue}
      </text>
    </g>
  );
};
