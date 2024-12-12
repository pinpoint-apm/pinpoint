import { useLocation } from 'react-router-dom';
import { getApplicationTypeAndName } from '@pinpoint-fe/utils';
import { getDateRange, getSearchParameters } from './utils';

export const useUrlStatSearchParameters = () => {
  const { search, pathname } = useLocation();
  const application = getApplicationTypeAndName(pathname);
  const searchParameters = getSearchParameters(search);
  const dateRange = getDateRange(search, false);
  const agentId = searchParameters?.agentId;

  return { application, dateRange, agentId, searchParameters };
};
