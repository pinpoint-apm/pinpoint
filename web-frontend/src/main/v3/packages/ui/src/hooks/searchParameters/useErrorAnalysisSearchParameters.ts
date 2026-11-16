import { useLocation } from 'react-router';
import { getApplicationTypeAndName } from '@pinpoint-fe/ui/src/utils';
import { getDateRange, getSearchParameters } from './utils';

export const useErrorAnalysisSearchParameters = () => {
  const { search, pathname } = useLocation();
  const application = getApplicationTypeAndName(pathname);
  const searchParameters = getSearchParameters(search);
  const dateRange = getDateRange(search, false);
  const agentId = searchParameters?.agentId;
  const groupBy = decodeURIComponent(searchParameters?.groupBy || '');
  const parsedGroupBy = groupBy ? parseErrorGroupBy(groupBy) : undefined;
  // getSearchParameters가 이미 두 번 디코딩했다(URLSearchParams + decodeURIComponent). 여기서 한 번 더
  // 풀면 메시지 속 '%'가 URIError를 던지거나 '%22' 같은 글자가 '"'로 바뀌어 JSON이 깨진다.
  // 링크를 만드는 쪽(CallTree)이 그만큼 미리 인코딩해 싣는다.
  const transactionInfo = searchParameters?.transactionInfo || '';
  const parsedTransactionInfo = (() => {
    if (!transactionInfo) return undefined;
    try {
      return JSON.parse(transactionInfo);
    } catch (e) {
      return undefined;
    }
  })();
  const openErrorDetail = Boolean(searchParameters?.openErrorDetail);

  return {
    application,
    dateRange,
    agentId,
    groupBy,
    parsedGroupBy,
    transactionInfo,
    parsedTransactionInfo,
    openErrorDetail,
    searchParameters,
  };
};

const parseErrorGroupBy = (groupBy: string) => {
  return groupBy ? groupBy.split(',') : [];
};
