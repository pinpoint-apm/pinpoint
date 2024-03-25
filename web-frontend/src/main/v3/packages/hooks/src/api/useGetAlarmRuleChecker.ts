import useSWR from 'swr';
import { END_POINTS } from '@pinpoint-fe/constants';
import { swrConfigs } from './swrConfigs';

export const useGetAlarmRuleChecker = ({ disableFetch }: { disableFetch?: boolean }) => {
  return useSWR<string[]>(!disableFetch ? [`${END_POINTS.ALARM_RULE_CHECKER}`] : null, {
    ...swrConfigs,
    suspense: false,
  });
};
