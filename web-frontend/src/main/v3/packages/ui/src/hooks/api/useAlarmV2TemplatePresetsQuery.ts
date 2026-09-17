import { useQuery } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2TemplatePreset } from '@pinpoint-fe/ui/src/constants/types';
import { queryFn } from '@pinpoint-fe/ui/src/hooks/api/reactQueryHelper';

/**
 * Built-in preset catalog. It is served from the classpath and never changes at
 * runtime, so it is fetched once and kept.
 */
export const useAlarmV2TemplatePresetsQuery = (enabled = true) => {
  return useQuery<AlarmV2TemplatePreset.Response>({
    queryKey: [END_POINTS.ALARM_V2_TEMPLATE_PRESETS],
    queryFn: queryFn(END_POINTS.ALARM_V2_TEMPLATE_PRESETS),
    staleTime: Infinity,
    enabled,
  });
};
