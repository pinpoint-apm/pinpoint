import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { useAtomValue, useSetAtom } from 'jotai';
import { ChartsBoard, ChartsBoardProps } from '..';
import { END_POINTS, GetHistogramStatistics, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import {
  serverMapCurrentTargetDataAtom,
  currentNodeStatisticsAtom,
  currentServerAtom,
  serverMapDataAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { convertParamsToQueryString, getParsedDate } from '@pinpoint-fe/ui/src/utils';
import { useSearchParameters, useServerMapLinkedData } from '@pinpoint-fe/ui/src/hooks';
import { queryFn } from '@pinpoint-fe/ui/src/hooks/api/reactQueryHelper';
import { useTranslation } from 'react-i18next';

export interface ServerChartsBoardFetcherProps extends ChartsBoardProps {
  disableFetch: boolean;
}

export const ServerChartsBoardFetcher = ({
  disableFetch,
  children,
  ...props
}: ServerChartsBoardFetcherProps) => {
  const { searchParameters } = useSearchParameters();
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom);
  const currentServer = useAtomValue(currentServerAtom);
  const setCurrentNodeStatistics = useSetAtom(currentNodeStatisticsAtom);
  const serverMapData = useAtomValue(serverMapDataAtom);
  const serverMapLinkedData = useServerMapLinkedData({
    serverMapData: serverMapData?.applicationMapData as GetServerMap.ApplicationMapData,
    currentTargetData: currentTargetData as GetServerMap.NodeData,
  });
  const queryParams: GetHistogramStatistics.Parameters = {
    applicationName: (currentTargetData as GetServerMap.NodeData)?.applicationName,
    serviceTypeName: (currentTargetData as GetServerMap.NodeData)?.serviceType,
    from: getParsedDate(searchParameters.from).getTime(),
    to: getParsedDate(searchParameters.to).getTime(),
    fromApplicationNames: serverMapLinkedData?.from
      .map(({ applicationName }) => encodeURIComponent(applicationName))
      .join(','),
    fromServiceTypeCodes: serverMapLinkedData?.from
      .map(({ serviceTypeCode }) => serviceTypeCode)
      .join(','),

    toApplicationNames: serverMapLinkedData?.to
      .map(({ applicationName }) => encodeURIComponent(applicationName))
      .join(','),

    toServiceTypeCodes: serverMapLinkedData?.to
      .map(({ serviceTypeCode }) => serviceTypeCode)
      .join(','),
  };
  const { t } = useTranslation();

  const getQueryString = React.useCallback(() => {
    if (
      queryParams.from &&
      queryParams.to &&
      queryParams.applicationName &&
      queryParams.serviceTypeName &&
      (queryParams.fromApplicationNames ||
        queryParams.fromServiceTypeCodes ||
        queryParams.toApplicationNames ||
        queryParams.toServiceTypeCodes)
    ) {
      return '?' + convertParamsToQueryString(queryParams);
    }

    return '';
  }, [queryParams]);

  const { data } = useQuery<GetHistogramStatistics.Response | null>({
    queryKey: [END_POINTS.HISTOGRAM_STATISTICS, queryParams],
    queryFn: queryFn(`${END_POINTS.HISTOGRAM_STATISTICS}${getQueryString()}`),
    enabled: !!getQueryString() && !disableFetch,
  });

  React.useEffect(() => {
    if (data) {
      setCurrentNodeStatistics(data);
    }
  }, [data]);

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
      timestamp={
        serverMapData?.applicationMapData?.timestamp as GetServerMap.ApplicationMapData['timestamp']
      }
      nodeData={serverData as unknown as GetServerMap.NodeData} // todo
      chartsContainerClassName="pt-12"
      emptyMessage={t('COMMON.NO_DATA')}
    >
      {children}
    </ChartsBoard>
  );
};
