import React from 'react';
import useSWR from 'swr';
import { useAtomValue, useSetAtom } from 'jotai';
import { ChartsBoard, ChartsBoardProps, ChartBoardSkeleton } from '@pinpoint-fe/ui';
import { END_POINTS, GetResponseTimeHistogram, GetServerMap } from '@pinpoint-fe/constants';
import {
  serverMapCurrentTargetDataAtom,
  currentNodeStatisticsAtom,
  currentServerAtom,
} from '@pinpoint-fe/atoms';
import { convertParamsToQueryString, getParsedDate } from '@pinpoint-fe/utils';
import { useSearchParameters, swrConfigs } from '@pinpoint-fe/hooks';
import { useTranslation } from 'react-i18next';
import { ErrorBoundary } from '@pinpoint-fe/ui';

export interface ServerChartsBoardFetcherProps extends ChartsBoardProps {
  disableFetch: boolean;
}

const ServerChartsBoard = ({ disableFetch, children, ...props }: ServerChartsBoardFetcherProps) => {
  const { searchParameters, search } = useSearchParameters();
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom);
  const currentServer = useAtomValue(currentServerAtom);
  const setCurrentNodeStatistics = useSetAtom(currentNodeStatisticsAtom);
  const [queryParams, setQueryParams] = React.useState<GetResponseTimeHistogram.Parameters>({
    applicationName: (currentTargetData as GetServerMap.NodeData)?.applicationName,
    serviceTypeCode: (currentTargetData as GetServerMap.NodeData)?.serviceTypeCode,
    from: getParsedDate(searchParameters.from).getTime(),
    to: getParsedDate(searchParameters.to).getTime(),
  });
  const { t } = useTranslation();

  const getQueryString = React.useCallback(() => {
    if (
      queryParams.from &&
      queryParams.to &&
      queryParams.applicationName &&
      queryParams.serviceTypeCode
    ) {
      return '?' + convertParamsToQueryString(queryParams);
    }

    return '';
  }, [queryParams]);

  const { data } = useSWR<GetResponseTimeHistogram.Response>(
    getQueryString() && !disableFetch
      ? [`${END_POINTS.RESPONSE_TIME_HISTOGRAM_DATA_V2}${getQueryString()}`]
      : null,
    swrConfigs,
  );

  React.useEffect(() => {
    setCurrentNodeStatistics(data);
  }, [data]);

  React.useEffect(() => {
    setQueryParams((prev: GetResponseTimeHistogram.Parameters) => ({
      ...prev,
      from: getParsedDate(searchParameters.from).getTime(),
      to: getParsedDate(searchParameters.to).getTime(),
    }));
  }, [search]);

  React.useEffect(() => {
    setQueryParams((prev: GetResponseTimeHistogram.Parameters) => ({
      ...prev,
      applicationName: (currentTargetData as GetServerMap.NodeData)?.applicationName,
      serviceTypeCode: (currentTargetData as GetServerMap.NodeData)?.serviceTypeCode,
    }));
  }, [currentTargetData]);

  const getServerData = React.useCallback(() => {
    const agentId = currentServer?.agentId || '';
    return {
      histogram: data?.agentHistogram[agentId],
      responseStatistics: data?.agentResponseStatistics[agentId],
      timeSeriesHistogram: data?.agentTimeSeriesHistogram[agentId],
    };
  }, [data, currentServer?.agentId]);
  const serverData = getServerData();

  return (
    <ChartsBoard
      {...props}
      nodeData={serverData as GetServerMap.NodeData}
      chartsContainerClassName="pt-12"
      emptyMessage={t('COMMON.NO_DATA')}
    >
      {children}
    </ChartsBoard>
  );
};

export const ServerChartsBoardFetcher = (props: ServerChartsBoardFetcherProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ChartBoardSkeleton />}>
        <ServerChartsBoard {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
