import React from 'react';
import useSWR from 'swr';
import { END_POINTS, ErrorAnalysisGroupedErrorList } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { useErrorAnalysisSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

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

  const { data, isLoading, isValidating } = useSWR<ErrorAnalysisGroupedErrorList.Response>(
    [queryString ? `${END_POINTS.ERROR_ANALYSIS_GROUPED_ERROR_LIST}${queryString}` : null],
    swrConfigs,
  );

  return { data, isLoading, isValidating };
};
