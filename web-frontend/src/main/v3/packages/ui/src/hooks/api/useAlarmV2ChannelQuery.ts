import { useQuery } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2Channel, AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';
import { queryFn } from '@pinpoint-fe/ui/src/hooks/api/reactQueryHelper';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';

// No service in the query keys: serviceScopedQueryKeyHashFn already splits every cache
// entry outside SERVICE_AGNOSTIC_ENDPOINTS by the service the request resolves to, and
// these endpoints are not in that list.
export const useAlarmV2ChannelQuery = () => {
  return useQuery<AlarmV2Channel.Response>({
    queryKey: [END_POINTS.ALARM_V2_CHANNEL],
    queryFn: queryFn(END_POINTS.ALARM_V2_CHANNEL),
  });
};

export const useAlarmV2ChannelsByRuleQuery = (
  ruleId?: number,
  application?: AlarmV2Rule.Parameters,
) => {
  const queryString = convertParamsToQueryString({ ...application });

  return useQuery<AlarmV2Channel.Response>({
    queryKey: [END_POINTS.ALARM_V2_CHANNEL, 'rule', ruleId, queryString],
    queryFn: queryFn(`${END_POINTS.ALARM_V2_CHANNEL}/rule/${ruleId}?${queryString}`),
    enabled: !!ruleId && !!application?.applicationName,
  });
};

export const useAlarmV2ChannelsByTemplateQuery = (templateId?: number) => {
  return useQuery<AlarmV2Channel.Response>({
    queryKey: [END_POINTS.ALARM_V2_CHANNEL, 'template', templateId],
    queryFn: queryFn(`${END_POINTS.ALARM_V2_CHANNEL}/template/${templateId}`),
    enabled: !!templateId,
  });
};
