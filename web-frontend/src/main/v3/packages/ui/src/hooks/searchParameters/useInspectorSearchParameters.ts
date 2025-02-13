import { useLocation } from 'react-router-dom';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';
import { getDateRange, getSearchParameters } from './utils';

export const useInspectorSearchParameters = () => {
  const { search, pathname } = useLocation();
  const application = getApplicationTypeAndName(pathname);
  const searchParameters = getSearchParameters(search);
  const dateRange = getDateRange(search, false);
  const agentId = searchParameters?.agentId;
  const version = searchParameters?.version;

  return { application, dateRange, agentId, version, searchParameters };
};
