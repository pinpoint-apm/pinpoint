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
  const queryParams = {
    applicationName: 'minwoo_local_app', // * temp
    from: 1723616400000, // * temp
    to: 1723617000000, // * temp
    metricGroupName,
    metricName,
    chartType,
    tags,
    aggregationFunction,
    fieldNameList: fieldNameList.join(','),
  };
  //   const { application, dateRange } = useOpenTelemetrySearchParameters();
  const queryString = getQueryString(queryParams);

  const { data, isLoading, refetch } = useSuspenseQuery<OtlpMetricData.Response | null>({
    queryKey: [END_POINTS.OTLP_METRIC_DATA, queryString],
    queryFn: !!queryString ? queryFn(`${END_POINTS.OTLP_METRIC_DATA}${queryString}`) : () => null,
  });

  return { data, isLoading, refetch };
};
