import useSWR from 'swr';
import {
  END_POINTS,
  InspectorAgentStatusTimelineType as InspectorAgentStatusTimeline,
  MAX_DATE_RANGE,
} from '@pinpoint-fe/ui/src/constants';
import { swrConfigs } from './swrConfigs';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useInspectorSearchParameters } from '../searchParameters';

const getQueryString = (queryParams: Partial<InspectorAgentStatusTimeline.Parameters>) => {
  if (queryParams.agentId && queryParams.from && queryParams.to) {
    return `?${convertParamsToQueryString(queryParams)}`;
  }
  return '';
};

export const useGetInspectorAgentStatusTimeline = () => {
  const { application, dateRange, agentId } = useInspectorSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const queryParams = {
    applicationName: application?.applicationName,
    agentId,
    from: calcFrom(from, to),
    to,
    exclude: '10199', // it's derived from the legacy
  };
  const queryString = getQueryString(queryParams);

  const { data } = useSWR<InspectorAgentStatusTimeline.Response>(
    queryString ? `${END_POINTS.INSPECTOR_AGENT_STATUS_TIMELINE}${queryString}` : null,
    swrConfigs,
  );

  return {
    data,
    totalRange: [queryParams.from, queryParams.to] as [number, number],
    activeRange: [from, to] as [number, number],
  };
};

const calcFrom = (from: number, to: number) => {
  const rangeDiff = to - from;
  const adjustedRangeDiff = rangeDiff * 3;

  return adjustedRangeDiff < MAX_DATE_RANGE.INSPECTOR
    ? to - adjustedRangeDiff
    : to - MAX_DATE_RANGE.INSPECTOR;
};
