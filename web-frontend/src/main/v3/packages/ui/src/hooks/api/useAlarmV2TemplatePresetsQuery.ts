import { useQuery } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2TemplatePreset } from '@pinpoint-fe/ui/src/constants/types';
import { queryFn } from '@pinpoint-fe/ui/src/hooks/api/reactQueryHelper';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';

/**
 * Built-in preset catalog. It is served from the classpath and never changes at
 * runtime, so it is fetched once and kept. With an application type, only the presets
 * a rule on that type may use.
 */
export const useAlarmV2TemplatePresetsQuery = (enabled = true, applicationType?: string) => {
  const url = applicationType
    ? `${END_POINTS.ALARM_V2_TEMPLATE_PRESETS}?${convertParamsToQueryString({ applicationType })}`
    : END_POINTS.ALARM_V2_TEMPLATE_PRESETS;
  return useQuery<AlarmV2TemplatePreset.Response>({
    queryKey: [END_POINTS.ALARM_V2_TEMPLATE_PRESETS, applicationType],
    queryFn: queryFn(url),
    staleTime: Infinity,
    enabled,
  });
};
