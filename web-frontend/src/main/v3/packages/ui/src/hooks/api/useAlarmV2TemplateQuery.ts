import { useQuery } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2Template } from '@pinpoint-fe/ui/src/constants/types';
import { queryFn } from '@pinpoint-fe/ui/src/hooks/api/reactQueryHelper';

export const useAlarmV2TemplateQuery = () => {
  return useQuery<AlarmV2Template.Response>({
    queryKey: [END_POINTS.ALARM_V2_TEMPLATE],
    queryFn: queryFn(END_POINTS.ALARM_V2_TEMPLATE),
  });
};
