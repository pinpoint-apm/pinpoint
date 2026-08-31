import { useLocation } from 'react-router';
import {
  getApplicationTypeAndName,
  getServiceNameFromPath,
  parseFilterStateFromQueryString,
} from '@pinpoint-fe/ui/src/utils';
import { getSearchParameters, getDateRange } from './utils';

export const useFilteredMapParameters = () => {
  const { search, pathname } = useLocation();
  const searchParameters = getSearchParameters(search);
  const application = getApplicationTypeAndName(pathname);
  /**
   * 경로에 실려 있는 service 이름. 없으면 undefined다.
   *
   * 전역 선택값으로 폴백하지 않는다(`useServiceNameForLink`와 다른 점). 이 값은 "servicemap에서
   * 넘어온 화면인가"를 가리키고, 화면은 그것으로 돌아갈 map과 이어질 링크의 형태를 정한다.
   * 폴백하면 servermap에서 넘어온 화면도 servicemap에서 온 것처럼 보인다.
   *
   * 조회 자체의 service는 이 값과 별개로 요청 인터셉터가 정한다(`resolveRequestService`).
   * 규칙이 같아서 결과도 같다 — 경로에 실려 있으면 그것, 없으면 전역 선택값.
   */
  const serviceName = getServiceNameFromPath(pathname);
  const dateRange = getDateRange(search, false);
  const parsedFilters = parseFilterStateFromQueryString(searchParameters.filter);
  const parsedHint = (() => {
    if (!searchParameters?.hint) return null;
    try {
      return JSON.parse(searchParameters?.hint);
    } catch (e) {
      return null;
    }
  })();

  return {
    search,
    dateRange,
    searchParameters,
    application,
    serviceName,
    parsedFilters,
    parsedHint,
  };
};
