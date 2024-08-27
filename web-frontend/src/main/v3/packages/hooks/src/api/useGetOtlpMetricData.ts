import { END_POINTS, OtlpMetricData, OtlpMetricDefUserDefined } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';
// import { useOpenTelemetrySearchParameters } from '../searchParameters';

const getQueryString = (queryParams: Partial<OtlpMetricData.Parameters>) => {
  if (queryParams.applicationName && queryParams.metricGroupName && queryParams.metricName) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

export const useGetOtlpMetricData = ({
  metricGroupName,
  metricName,
  chartType,
  tags,
  aggregationFunction,
  fieldNameList,
}: OtlpMetricDefUserDefined.Metric) => {
  const mockParam = {
    applicationName: 'minwoo_local_app',
    from: 1723616400000,
    to: 1723617000000,
    metricGroupName: 'jvm',
    metricName: 'memory',
    chartType: 'spline',
    tags: 'area:nonheap,pinpoint.agentId:minwoo_local_agent2,telemetry.sdk.language:java,id:Compressed Class Space,telemetry.sdk.version:1.12.4,telemetry.sdk.name:io.micrometer',
    aggregationFunction: 'avg',
    // fieldNameList: fieldNameList.join(','),
    fieldNameList: 'used,max',
  };
  const realParam = {
    applicationName: 'minwoo_local_app',
    from: 1723616400000,
    to: 1723617000000,
    metricGroupName,
    metricName,
    chartType,
    tags,
    aggregationFunction,
    fieldNameList: fieldNameList.join(','),
  };
  //   const { application, dateRange } = useOpenTelemetrySearchParameters();
  // TODO: 일단 mockParam 데이터로 차트 그려보기. realParam으로 안되는건 나중에 여쭤보기
  const queryString = getQueryString(mockParam);
  // const queryString = getQueryString(realParam);

  const { data, isLoading, refetch } = useSuspenseQuery<OtlpMetricData.Response | null>({
    queryKey: [END_POINTS.OTLP_METRIC_DATA, queryString],
    queryFn: !!queryString ? queryFn(`${END_POINTS.OTLP_METRIC_DATA}${queryString}`) : () => null,
  });

  return { data, isLoading, refetch };
};
