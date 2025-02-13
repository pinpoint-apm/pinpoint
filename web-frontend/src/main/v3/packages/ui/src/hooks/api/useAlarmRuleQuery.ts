import { AlarmRule, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useQuery, useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<AlarmRule.Parameters>) => {
  if (queryParams.applicationId) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

export const useAlarmRuleQuery = ({
  applicationId,
  suspense,
}: {
  applicationId?: string;
  suspense?: boolean;
}) => {
  const queryString = getQueryString({ applicationId });
  const query = suspense ? useSuspenseQuery : useQuery;

  const { data, isLoading, refetch } = query({
    queryKey: [END_POINTS.ALARM_RULE, queryString],
    queryFn: !!queryString ? queryFn(`${END_POINTS.ALARM_RULE}${queryString}`) : () => null,
    staleTime: 30000,
  });

  return { data, isLoading, refetch };
};
