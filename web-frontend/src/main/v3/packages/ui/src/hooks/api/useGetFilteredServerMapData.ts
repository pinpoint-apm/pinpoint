import React from 'react';
import useSWR from 'swr';
import { useUpdateEffect } from 'usehooks-ts';
import { FilteredMapType as FilteredMap, END_POINTS } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useFilteredMapParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';

const getQueryString = (queryParams: Partial<FilteredMap.Parameters>) => {
  if (
    queryParams.applicationName &&
    queryParams.serviceTypeName &&
    queryParams.from &&
    queryParams.to &&
    queryParams.filter
  ) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetFilteredServerMapData = (isPaused: boolean) => {
  const { dateRange, application, search, searchParameters } = useFilteredMapParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const defaultPartialOptions = {
    applicationName: application?.applicationName,
    serviceTypeName: application?.serviceType,
    from,
    to,
    originTo: to,
    calleeRange: 1,
    callerRange: 1,
    v: 4,
    limit: 5000,
    xGroupUnit: 987,
    yGroupUnit: 57,
    useStatisticsAgentState: false,
  };
  const [queryParams, setQueryParams] = React.useState<Partial<FilteredMap.Parameters>>({
    ...defaultPartialOptions,
    filter: searchParameters.filter,
    hint: searchParameters.hint,
  });
  const queryString = getQueryString(queryParams);
  const { data, isLoading } = useSWR<FilteredMap.Response>(
    [!isPaused && queryString ? `${END_POINTS.FILTERED_SERVER_MAP_DATA}${queryString}` : null],
    {
      ...swrConfigs,
      keepPreviousData: true,
    },
  );

  useUpdateEffect(() => {
    setQueryParams({
      ...defaultPartialOptions,
      filter: searchParameters.filter,
      hint: searchParameters.hint,
    });
  }, [search]);

  return { data, isLoading, setQueryParams };
};
