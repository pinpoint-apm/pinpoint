import { useQuery } from '@tanstack/react-query';
import { END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { AlarmV2Catalog } from '@pinpoint-fe/ui/src/constants/types';
import { queryFn } from '@pinpoint-fe/ui/src/hooks/api/reactQueryHelper';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';

/** With an application type, only the data sources a rule on that type may use. */
export const useAlarmV2DataSourcesQuery = (applicationType?: string) => {
  const url = applicationType
    ? `${END_POINTS.ALARM_V2_DATASOURCES}?${convertParamsToQueryString({ applicationType })}`
    : END_POINTS.ALARM_V2_DATASOURCES;
  return useQuery<AlarmV2Catalog.DataSourceDefinition[]>({
    queryKey: [END_POINTS.ALARM_V2_DATASOURCES, applicationType],
    queryFn: queryFn(url),
    staleTime: Infinity, // 카탈로그는 배포 전까지 변하지 않음
  });
};

export const useAlarmV2MetricsQuery = (dataSource?: string) => {
  return useQuery<AlarmV2Catalog.MetricDefinition[]>({
    queryKey: [END_POINTS.ALARM_V2_METRICS, dataSource],
    queryFn: queryFn(
      `${END_POINTS.ALARM_V2_METRICS}?${convertParamsToQueryString({ dataSource: dataSource! })}`,
    ),
    enabled: !!dataSource,
    staleTime: Infinity,
  });
};
