import { useQuery } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { queryFn } from './reactQueryHelper';

export const useGetAlarmRuleChecker = ({ disableFetch }: { disableFetch?: boolean }) => {
  return useQuery<string[]>({
    queryKey: [END_POINTS.ALARM_RULE_CHECKER],
    queryFn: queryFn(`${END_POINTS.ALARM_RULE_CHECKER}`),
    enabled: !disableFetch,
  });
};
