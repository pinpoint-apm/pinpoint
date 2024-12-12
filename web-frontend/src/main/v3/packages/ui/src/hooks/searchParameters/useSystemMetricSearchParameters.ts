import { useLocation } from 'react-router-dom';
import { getDateRange, getSearchParameters } from './utils';

export const useSystemMetricSearchParameters = () => {
  const { search, pathname } = useLocation();
  const searchParameters = getSearchParameters(search);
  const hostGroupName = pathname.match(/\/(?<pageRoute>.+)\/(?<hostGroupName>.+)/)?.groups
    ?.hostGroupName;
  const dateRange = getDateRange(search, false);
  const hostName = searchParameters?.hostName;

  return {
    dateRange,
    hostGroupName,
    hostName,
    searchParameters,
  };
};
