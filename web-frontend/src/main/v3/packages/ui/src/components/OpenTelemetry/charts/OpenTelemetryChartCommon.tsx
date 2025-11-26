import {
  CartesianGrid,
  XAxis,
  YAxis,
  XAxisProps,
  YAxisProps,
  CartesianGridProps,
  LegendProps,
  TooltipProps,
} from 'recharts';
import { ChartLegend, ChartLegendContent, ChartTooltip } from '../../ui/chart';
import { OpenTelemetryChartTooltipContent } from './OpenTelemetryChartTooltipContent';

export interface OpenTelemetryChartCommonProps {
  gridConfig?: CartesianGridProps;
  xAxisConfig?: XAxisProps;
  yAxisConfig?: YAxisProps;
  legendConfig?: LegendProps;
  tooltipConfig?: TooltipProps<number, string> & { showTotal?: boolean };
}

export const OpenTelemetryChartCommon = (
  { gridConfig, xAxisConfig, yAxisConfig, tooltipConfig }: OpenTelemetryChartCommonProps,
  chartContainerRef: React.RefObject<HTMLDivElement | null>,
) => {
  const { showTotal, ...restTooltipConfig } = tooltipConfig || {};
  const chartWidth = chartContainerRef?.current?.offsetWidth || 392;

  // https://github.com/recharts/recharts/issues/5739
  // https://github.com/recharts/recharts/issues/2462#issuecomment-2715233201
  return [
    <CartesianGrid key="grid" vertical={false} {...gridConfig} />,
    <XAxis
      key="xaxis"
      className="text-xxs"
      tickMargin={5}
      padding={{ left: 10, right: 10 }}
      {...xAxisConfig}
    />,
    <YAxis
      key="yaxis"
      className="text-xxs"
      axisLine={false}
      tickLine={false}
      tickMargin={5}
      {...yAxisConfig}
    />,
    <ChartTooltip
      key="tooltip"
      content={
        <OpenTelemetryChartTooltipContent
          indicator="line"
          labelFormatter={(label) => xAxisConfig?.tickFormatter?.(label, 0) || ''}
          formatter={yAxisConfig?.tickFormatter}
          showTotal={showTotal}
          chartWidth={chartWidth}
        />
      }
      {...restTooltipConfig}
    />,
    <ChartLegend key="legend" content={<ChartLegendContent />} />,
  ];
};
