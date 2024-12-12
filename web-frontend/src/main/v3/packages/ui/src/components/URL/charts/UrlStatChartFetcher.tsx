import { useAtomValue } from 'jotai';
import { urlSelectedSummaryDataAtom } from '@pinpoint-fe/atoms';
import { useGetUrlStatChartData } from '@pinpoint-fe/ui/hooks';
import { UrlStatTotalCountChart } from './UrlStatTotalCountChart';
import { UrlStatFailureCountChart } from './UrlStatFailureCountChart';
import { UrlStatApdexChart } from './UrlStatApdexChart';
import { UrlStatLatencyChart } from './UrlStatLatencyChart';
import { UrlStatDefaultChart } from './UrlStatDefaultChart';

export interface UrlStatChartFetcherProps {
  type?: string;
  emptyMessage?: string;
  guideMessage?: string;
}

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
      return <UrlStatTotalCountChart data={data} emptyMessage={emptyMessage} />;
    case 'failure':
      return <UrlStatFailureCountChart data={data} emptyMessage={emptyMessage} />;
    case 'apdex':
      return <UrlStatApdexChart data={data} emptyMessage={emptyMessage} />;
    case 'latency':
      return <UrlStatLatencyChart data={data} emptyMessage={emptyMessage} />;
  }
  return null;
};
