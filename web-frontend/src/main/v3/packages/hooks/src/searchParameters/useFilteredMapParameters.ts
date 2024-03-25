import { useLocation } from 'react-router-dom';
import { getApplicationTypeAndName, parseFilterStateFromQueryString } from '@pinpoint-fe/utils';
import { getSearchParameters, getDateRange } from './utils';

export const useFilteredMapParameters = () => {
  const { search, pathname } = useLocation();
  const searchParameters = getSearchParameters(search);
  const application = getApplicationTypeAndName(pathname);
  const dateRange = getDateRange(search, false);
  const parsedFilters = parseFilterStateFromQueryString(searchParameters.filter);
  const parsedHint = searchParameters?.hint ? JSON.parse(searchParameters.hint) : null;

  return { search, dateRange, searchParameters, application, parsedFilters, parsedHint };
};
