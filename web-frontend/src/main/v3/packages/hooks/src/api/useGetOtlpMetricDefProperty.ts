import { END_POINTS, OtlpMetricDefProperty } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
// import { useOpenTelemetrySearchParameters } from '../searchParameters';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<OtlpMetricDefProperty.Parameters>) => {
  if (queryParams.applicationName) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

export const useGetOtlpMetricDefProperty = () => {
  // const { application } = useOpenTelemetrySearchParameters();
  const queryString = getQueryString({ applicationName: '00000000-0000-0000-0000-000000000000' });

  const { data, isLoading, refetch } = useSuspenseQuery<OtlpMetricDefProperty.Response | null>({
    queryKey: [END_POINTS.OTLP_METRIC_DEF_PROPERTY, queryString],
    queryFn: !!queryString
      ? queryFn(`${END_POINTS.OTLP_METRIC_DEF_PROPERTY}${queryString}`)
      : () => null,
  });

  return { data, isLoading, refetch };
};
