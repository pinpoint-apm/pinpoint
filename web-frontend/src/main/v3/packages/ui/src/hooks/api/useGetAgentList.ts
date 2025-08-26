import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, SearchApplication } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useSearchParameters } from '../searchParameters';
import { getDateRange } from '../searchParameters/utils';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<SearchApplication.Parameters>) => {
  if (queryParams.from && queryParams.to && queryParams.application && queryParams.sortBy) {
    return '?' + convertParamsToQueryString(queryParams);
  }

  return '';
};

export enum AGENT_LIST_SORT {
  ID = 'AGENT_ID_ASC',
  NAME = 'AGENT_NAME_ASC',
  RECENT = 'RECENT',
}

interface UseGetAgentListProps {
  sortBy?: AGENT_LIST_SORT;
}

export const useGetAgentList = ({ sortBy = AGENT_LIST_SORT.ID }: UseGetAgentListProps) => {
  const { search, application } = useSearchParameters();
  const dateRange = getDateRange(search, false);
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const applicationName = application?.applicationName;
  const serviceTypeName = application?.serviceType;

  const queryString = getQueryString({
    from,
    to,
    application: applicationName,
    serviceTypeName,
    sortBy,
  });

  const { data, isLoading } = useSuspenseQuery<SearchApplication.Response | null>({
    queryKey: [END_POINTS.SEARCH_APPLICATION, queryString],
    queryFn: !!queryString ? queryFn(`${END_POINTS.SEARCH_APPLICATION}${queryString}`) : () => null,
  });

  return { data, isLoading };
};
