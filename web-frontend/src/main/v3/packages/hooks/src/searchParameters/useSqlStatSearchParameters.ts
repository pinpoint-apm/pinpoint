import { useLocation } from 'react-router-dom';
import { getApplicationTypeAndName } from '@pinpoint-fe/utils';
import { getDateRange, getSearchParameters } from './utils';

export const useSqlStatSearchParameters = () => {
  const { search, pathname } = useLocation();
  const application = getApplicationTypeAndName(pathname);
  const searchParameters = getSearchParameters(search);
  const query = searchParameters?.query || '';
  const groupBy = searchParameters?.groupBy;
  const dateRange = getDateRange(search, false);
  const parsedQuery = query ? parseSqlQuery(query) : undefined;

  return { application, dateRange, query, groupBy, parsedQuery, searchParameters };
};

const parseSqlQuery = (query: string) => {
  const result: { [key: string]: string[] } = {};
  const items = query.split(',');

  items.forEach((item) => {
    const [key, value] = item.split(':');

    if (!result[key]) {
      result[key] = [];
    }
    result[key].push(value);
  });

  return result;
};
