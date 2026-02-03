import { useAtomValue } from 'jotai';
import { urlSelectedSummaryDataAtom } from '@pinpoint-fe/ui/src/atoms';
import { useGetUrlStatChartData } from '@pinpoint-fe/ui/src/hooks';
import { UrlStatDefaultChart } from './UrlStatDefaultChart';
import { UrlStatEChartsChart } from './UrlStatEChartsChart';
import { colors } from '@pinpoint-fe/ui/src/constants';
import {
  abbreviateNumber,
  addCommas,
  numberInDecimal,
  numberInInteger,
} from '@pinpoint-fe/ui/src/utils';

export interface UrlStatChartFetcherProps {
  type?: string;
  emptyMessage?: string;
  guideMessage?: string;
}
const barChartColors = [
  colors.emerald[300],
  colors.emerald[400],
  colors.emerald[500],
  colors.emerald[600],
  colors.blue[300],
  colors.orange[300],
  colors.orange[500],
  colors.red[500],
];

export const UrlStatChartFetcher = ({
  type = 'total',
  emptyMessage,
  guideMessage,
}: UrlStatChartFetcherProps) => {
  const selectedSummaryData = useAtomValue(urlSelectedSummaryDataAtom);
  const { data } = useGetUrlStatChartData({ type, uri: selectedSummaryData?.uri });
  const isEmpty = data?.metricValueGroups[0].metricValues.length === 0;

  if (!selectedSummaryData?.uri || isEmpty) {
    return <UrlStatDefaultChart emptyMessage={guideMessage} />;
  }

  switch (type) {
    case 'total':
      return (
        <UrlStatEChartsChart
          chartType="bar"
          yAxisName="Total Count"
          chartColors={barChartColors}
          formatter={(value: number) => abbreviateNumber(value, ['', 'K', 'M', 'G'])}
          data={data}
          emptyMessage={emptyMessage}
        />
      );
    case 'failure':
      return (
        <UrlStatEChartsChart
          chartType="bar"
          yAxisName="Failure Count"
          chartColors={barChartColors}
          formatter={(value: number) => abbreviateNumber(value, ['', 'K', 'M', 'G'])}
          data={data}
          emptyMessage={emptyMessage}
        />
      );
    case 'apdex':
      return (
        <UrlStatEChartsChart
          chartType="line"
          yAxisName="Apdex Score"
          chartColors={[colors.green[400]]}
          formatter={(value: number) => numberInDecimal(value, 2)}
          data={data}
          emptyMessage={emptyMessage}
        />
      );
    case 'latency':
      return (
        <UrlStatEChartsChart
          chartType="line"
          yAxisName="Latency (ms)"
          chartColors={[colors.violet[800], colors.blue[600]]}
          formatter={(value: number) => `${addCommas(numberInInteger(value))}ms`}
          data={data}
          emptyMessage={emptyMessage}
        />
      );
  }
  return null;
};
