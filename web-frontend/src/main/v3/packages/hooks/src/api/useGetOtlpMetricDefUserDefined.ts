import { END_POINTS, OtlpMetricDefUserDefined } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { useOpenTelemetrySearchParameters } from '../searchParameters';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<OtlpMetricDefUserDefined.GetParameters>) => {
  if (queryParams.applicationName) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

export const useGetOtlpMetricDefUserDefined = () => {
  const { application } = useOpenTelemetrySearchParameters();
  const queryString = getQueryString({ applicationName: application?.applicationName });

  const { data, isLoading, refetch } =
    useSuspenseQuery<OtlpMetricDefUserDefined.GetResponse | null>({
      queryKey: [END_POINTS.OTLP_METRIC_DEF_USER_DEFINED, queryString],
      queryFn: !!queryString
        ? queryFn(`${END_POINTS.OTLP_METRIC_DEF_USER_DEFINED}${queryString}`)
        : () => null,
    });

  return { data, isLoading, refetch };
};
