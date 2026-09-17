import { useQuery } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2Rule } from '@pinpoint-fe/ui/src/constants/types';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { queryFn } from '@pinpoint-fe/ui/src/hooks/api/reactQueryHelper';

export const useAlarmV2RuleQuery = (params: AlarmV2Rule.Parameters) => {
  const queryString = convertParamsToQueryString(params);
  return useQuery<AlarmV2Rule.Response>({
    queryKey: [END_POINTS.ALARM_V2_RULE, queryString],
    queryFn: queryFn(`${END_POINTS.ALARM_V2_RULE}?${queryString}`),
    enabled: !!params.applicationName,
  });
};

export const useAlarmV2RuleDetailQuery = (
  ruleId?: number,
  application?: AlarmV2Rule.Parameters,
) => {
  const queryString = convertParamsToQueryString({ ...application });

  return useQuery<AlarmV2Rule.RuleData>({
    queryKey: [END_POINTS.ALARM_V2_RULE, ruleId, queryString],
    queryFn: queryFn(`${END_POINTS.ALARM_V2_RULE}/${ruleId}?${queryString}`),
    enabled: !!ruleId && !!application?.applicationName,
  });
};

export const useAlarmV2RuleStateQuery = (ruleId?: number, application?: AlarmV2Rule.Parameters) => {
  const queryString = convertParamsToQueryString({ ...application });

  return useQuery<AlarmV2Rule.StateResponse>({
    queryKey: [END_POINTS.ALARM_V2_RULE, ruleId, 'state', queryString],
    queryFn: queryFn(`${END_POINTS.ALARM_V2_RULE}/${ruleId}/state?${queryString}`),
    enabled: !!ruleId && !!application?.applicationName,
  });
};

export const useAlarmV2RuleHistoryQuery = (
  ruleId?: number,
  application?: AlarmV2Rule.Parameters,
  limit = 50,
) => {
  const queryString = convertParamsToQueryString({ ...application, limit });

  return useQuery<AlarmV2Rule.HistoryEntry[]>({
    queryKey: [END_POINTS.ALARM_V2_RULE, ruleId, 'history', queryString],
    queryFn: queryFn(`${END_POINTS.ALARM_V2_RULE}/${ruleId}/history?${queryString}`),
    enabled: !!ruleId && !!application?.applicationName,
  });
};
