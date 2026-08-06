import { getApplicationTypeAndName, getServiceNameFromPath } from '@pinpoint-fe/ui/src/utils';
import { useSearchParameters } from './useSearchParameters';
import { getDateRange } from './utils';

export const useTransactionSearchParameters = () => {
  const props = useSearchParameters();
  // transaction 화면들은 serviceName을 별도 세그먼트로 싣지만, application 세그먼트는 그대로
  // 마지막에 오므로 기본 파서로 읽는다. serviceName은 아래에서 따로 읽는다.
  const parsedApplication = getApplicationTypeAndName(props.pathname);
  const withFilter = props?.searchParameters?.withFilter === 'true' ? true : false;
  const dateRange = getDateRange(props?.search, false);
  const dragInfo = props?.searchParameters?.dragInfo;
  const parseDragInfo = (() => {
    if (!dragInfo) return null;
    try {
      return JSON.parse(dragInfo);
    } catch (e) {
      return null;
    }
  })();
  const transactionInfo = props.searchParameters?.transactionInfo;
  const parsedTransactionInfo = (() => {
    if (!transactionInfo) return null;
    try {
      return JSON.parse(transactionInfo);
    } catch (e) {
      return null;
    }
  })();
  // plain traceId string; when present the list panel is populated by a
  // /api/transaction/metadata lookup instead of heatmap drag / filter map
  const traceInfo = props.searchParameters?.traceInfo;
  return {
    ...props,
    application: parsedApplication,
    /** URL에 실려 있는 service 이름. 이 화면의 모든 API가 pServiceName 헤더로 이 값을 보낸다. */
    serviceName: getServiceNameFromPath(props.pathname),
    dateRange,
    withFilter,
    dragInfo: parseDragInfo,
    transactionInfo: parsedTransactionInfo,
    traceInfo,
  };
};
