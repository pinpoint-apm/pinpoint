import { Chart } from '@pinpoint-fe/ui/src/constants';
import { ComposedChart, ReferenceLine } from 'recharts';
import { useRechart } from '../ReChart/useRechart';
import { ChartContainer } from '../ui';

export interface MiniChart {
  chart: Chart;
}

export const MiniChart = ({ chart }: MiniChart) => {
  const { data, chartConfig, maxValue, renderChartChildComponents } = useRechart(chart);

  return (
    <ChartContainer config={chartConfig} className="w-full h-[40px]">
      <ComposedChart
        data={data}
        margin={{
          top: 6,
          right: 60,
          bottom: 6,
          left: 2,
        }}
      >
        {data?.length && maxValue > -1 && (
          <ReferenceLine
            y={maxValue}
            stroke="black"
            strokeDasharray="3 3"
            position="middle"
            label={{ position: 'right', value: Math.round(maxValue * 100) / 100 }}
            ifOverflow="extendDomain"
          />
        )}
        {renderChartChildComponents()}
      </ComposedChart>
    </ChartContainer>
  );
};
