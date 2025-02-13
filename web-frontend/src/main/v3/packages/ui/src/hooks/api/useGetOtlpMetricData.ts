import {
  END_POINTS,
  OtlpMetricData,
  OtlpMetricDefUserDefined,
} from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useSuspenseQuery } from '@tanstack/react-query';
import { queryFn } from './reactQueryHelper';
import { useOpenTelemetrySearchParameters } from '../searchParameters';

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
  primaryForFieldAndTagRelation,
  tagGroupList,
  fieldNameList,
  aggregationFunction,
}: OtlpMetricDefUserDefined.Metric) => {
  const { application, dateRange } = useOpenTelemetrySearchParameters();
  const queryParams = {
    applicationName: application?.applicationName,
    from: dateRange.from.getTime(),
    to: dateRange.to.getTime(),
    metricGroupName,
    metricName,
    chartType,
    primaryForFieldAndTagRelation,
    tagGroupList: tagGroupList?.join('&tagGroupList='),
    aggregationFunction,
    fieldNameList: fieldNameList?.join('&fieldNameList='),
  };
  const queryString = getQueryString(queryParams);

  const { data, isLoading, refetch } = useSuspenseQuery<OtlpMetricData.Response | null>({
    queryKey: [END_POINTS.OTLP_METRIC_DATA, queryString],
    queryFn: !!queryString ? queryFn(`${END_POINTS.OTLP_METRIC_DATA}${queryString}`) : () => null,
  });

  return { data, isLoading, refetch };
};
