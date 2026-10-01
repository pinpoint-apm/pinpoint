import { useLocation } from 'react-router';
import { parseSystemMetricPath } from '@pinpoint-fe/ui/src/utils';
import { useEnableServiceMap } from '../utility/useEnableServiceMap';
import { getDateRange, getSearchParameters } from './utils';

export const useSystemMetricSearchParameters = () => {
  const { search, pathname } = useLocation();
  const enableServiceMap = useEnableServiceMap();
  const searchParameters = getSearchParameters(search);
  // hostGroup 세그먼트는 serviceName과 모양으로 구별되지 않아, 경로 형태를 설정이 정한다.
  const { hostGroupName } = parseSystemMetricPath(pathname, enableServiceMap);
  const dateRange = getDateRange(search, false);
  const hostName = searchParameters?.hostName;

  return {
    dateRange,
    hostGroupName,
    hostName,
    searchParameters,
  };
};
