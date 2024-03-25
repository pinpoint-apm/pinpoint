import { useAtomValue } from 'jotai';
import { useGetSqlStatChartData } from '@pinpoint-fe/hooks';
import { sqlSelectedSummaryDatasAtom } from '@pinpoint-fe/atoms';
import { SqlStatSummary } from '@pinpoint-fe/constants';

import { SqlStatAvgTimeChart } from './SqlStatAvgTimeChart';
import { SqlStatMaxTimeChart } from './SqlStatMaxTimeChart';
import { SqlStatTotalCountChart } from './SqlStatTotalCountChart';
import { SqlStatTotalTimeChart } from './SqlStatTotalTimeChart';

export interface SqlStatChartFetcherProps {
  type?: string;
  emptyMessage?: string;
}

export const SqlStatChartFetcher = ({
  type = 'avgTime',
  emptyMessage,
}: SqlStatChartFetcherProps) => {
  const selectedSummaryDatas = useAtomValue(sqlSelectedSummaryDatasAtom);
  const selectedQueryList = selectedSummaryDatas.map(
    ({ id, label }: SqlStatSummary.SummaryData) => {
      return id ? `query:${id}` : `${label}`;
    },
  );
  const { data } = useGetSqlStatChartData({ type, selectedQueryList });

  switch (type) {
    case 'avgTime':
      return <SqlStatAvgTimeChart data={data} emptyMessage={emptyMessage} />;
    case 'maxTime':
      return <SqlStatMaxTimeChart data={data} emptyMessage={emptyMessage} />;
    case 'totalTime':
      return <SqlStatTotalTimeChart data={data} emptyMessage={emptyMessage} />;
    case 'totalCount':
      return <SqlStatTotalCountChart data={data} emptyMessage={emptyMessage} />;
  }
};
