import React from 'react';
import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, ErrorAnalysisGroupedErrorList } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useErrorAnalysisSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<ErrorAnalysisGroupedErrorList.Parameters>) => {
  if (queryParams.applicationName && queryParams.from && queryParams.to && queryParams.groupBy) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetErrorAnalysisGroupedErrorListData = () => {
  const { application, dateRange, agentId, groupBy } = useErrorAnalysisSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const applicationName = application?.applicationName;
  const [queryParams, setQueryParams] = React.useState<
    Partial<ErrorAnalysisGroupedErrorList.Parameters>
  >({
    applicationName,
    from,
    to,
    agentId,
    groupBy,
  });

  const queryString = getQueryString(queryParams);

  React.useEffect(() => {
    setQueryParams((prev) => ({
      ...prev,
      applicationName: application?.applicationName,
      from,
      to,
      agentId,
      groupBy,
    }));
  }, [application?.applicationName, application?.serviceType, from, to, agentId, groupBy]);

  const { data, isLoading, isFetching } =
    useSuspenseQuery<ErrorAnalysisGroupedErrorList.Response | null>({
      queryKey: [END_POINTS.ERROR_ANALYSIS_GROUPED_ERROR_LIST, queryString],
      queryFn: queryString
        ? queryFn(`${END_POINTS.ERROR_ANALYSIS_GROUPED_ERROR_LIST}${queryString}`)
        : () => null,
    });

  return { data, isLoading, isValidating: isFetching };
};
