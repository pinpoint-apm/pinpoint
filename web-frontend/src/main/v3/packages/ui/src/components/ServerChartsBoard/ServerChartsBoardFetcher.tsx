import React from 'react';
import useSWR from 'swr';
import { useAtomValue, useSetAtom } from 'jotai';
import { ChartsBoard, ChartsBoardProps } from '..';
import { END_POINTS, GetResponseTimeHistogram, GetServerMap } from '@pinpoint-fe/constants';
import {
  serverMapCurrentTargetDataAtom,
  currentNodeStatisticsAtom,
  currentServerAtom,
  serverMapDataAtom,
} from '@pinpoint-fe/ui/atoms';
import { convertParamsToQueryString, getParsedDate } from '@pinpoint-fe/utils';
import { useSearchParameters, swrConfigs, useServerMapLinkedData } from '@pinpoint-fe/ui/hooks';
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
  const queryParams: GetResponseTimeHistogram.Parameters = {
    applicationName: (currentTargetData as GetServerMap.NodeData)?.applicationName,
    serviceTypeCode: (currentTargetData as GetServerMap.NodeData)?.serviceTypeCode,
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
      queryParams.serviceTypeCode &&
      (queryParams.fromApplicationNames ||
        queryParams.fromServiceTypeCodes ||
        queryParams.toApplicationNames ||
        queryParams.toServiceTypeCodes)
    ) {
      return '?' + convertParamsToQueryString(queryParams);
    }

    return '';
  }, [queryParams]);

  const { data } = useSWR<GetResponseTimeHistogram.Response>(
    getQueryString() && !disableFetch
      ? `${END_POINTS.RESPONSE_TIME_HISTOGRAM_DATA_V2}${getQueryString()}`
      : null,
    swrConfigs,
  );

  React.useEffect(() => {
    setCurrentNodeStatistics(data);
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
      nodeData={serverData as GetServerMap.NodeData}
      chartsContainerClassName="pt-12"
      emptyMessage={t('COMMON.NO_DATA')}
    >
      {children}
    </ChartsBoard>
  );
};
