import { END_POINTS, OtlpMetricDefProperty } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useOpenTelemetrySearchParameters } from '../searchParameters';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<OtlpMetricDefProperty.Parameters>) => {
  if (queryParams.applicationName) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

export const useGetOtlpMetricDefProperty = () => {
  const { application } = useOpenTelemetrySearchParameters();
  const queryString = getQueryString({ applicationName: application?.applicationName });

  const { data, isLoading, refetch } = useSuspenseQuery<OtlpMetricDefProperty.Response | null>({
    queryKey: [END_POINTS.OTLP_METRIC_DEF_PROPERTY, queryString],
    queryFn: !!queryString
      ? queryFn(`${END_POINTS.OTLP_METRIC_DEF_PROPERTY}${queryString}`)
      : () => null,
  });

  return { data, isLoading, refetch };
};
