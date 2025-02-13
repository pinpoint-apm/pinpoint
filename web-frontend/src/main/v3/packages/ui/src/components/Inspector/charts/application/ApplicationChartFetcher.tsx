import { useGetInspectorApplicationChartData } from '@pinpoint-fe/ui/src/hooks';
import { useChartConfig } from '../../../../lib';
import { colors } from '@pinpoint-fe/ui/src/constants';
import { ChartCoreProps } from '../ChartCore';
import { ChartCore } from '../ChartCore';

export const APPLICATION_CHART_ID_LIST = [
  'heap',
  'nonHeap',
  'jvmCpu',
  'systemCpu',
  'apdex',
  'transaction',
  'activeTrace',
  'totalThreadCount',
  'responseTime',
  'fileDescriptor',
  'directCount',
  'directMemoryUsed',
  'mappedMemoryCount',
  'mappedMemoryUsed',
  'loadedClass',
  'unloadedClass',
] as const;

export type APPLICATION_CHART_ID = (typeof APPLICATION_CHART_ID_LIST)[number];

export interface ApplicationChartFetcherProps {
  chartId: APPLICATION_CHART_ID;
  className?: string;
  emptyMessage?: string;
  children?: (props: ChartCoreProps) => React.ReactNode;
}

export const ApplicationChartFetcher = ({
  chartId,
  children,
  ...props
}: ApplicationChartFetcherProps) => {
  const { data } = useGetInspectorApplicationChartData({
    metricDefinitionId: chartId,
  });
  const chartConfig = useChartConfig(data, {
    dataOptions: {
      colors: {
        AVG: colors.violet[800],
        MIN: colors.sky[500],
        MAX: colors.blue[800],
      },
      regions: {
        MIN: [
          {
            style: {
              dasharray: '2 2',
            },
          },
        ],
        MAX: [
          {
            style: {
              dasharray: '2 2',
            },
          },
        ],
      },
    },
  });

  if (chartConfig) {
    return typeof children === 'function' && data ? (
      children({ data, chartOptions: chartConfig.chartOptions, ...props })
    ) : (
      <ChartCore data={chartConfig.chartData} chartOptions={chartConfig.chartOptions} {...props} />
    );
  } else {
    return null;
  }
};
