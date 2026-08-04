import {
  getApplicationTypeAndNameFromPath,
  getServiceNameFromPath,
} from '@pinpoint-fe/ui/src/utils';
import { useSearchParameters } from './useSearchParameters';
import { getDateRange } from './utils';

export const useTransactionSearchParameters = () => {
  const props = useSearchParameters();
  // transactionList의 application 세그먼트는 `{serviceName}@{applicationName}@{serviceType}`
  // 형태일 수 있어, 기본 파서(useSearchParameters)로는 applicationName에 serviceName이 섞인다.
  // 이 훅은 transactionDetail에서도 쓰이므로, serviceName 분리는 그것을 싣는 경로에서만
  // 해야 한다(`hasServiceNameInPath`). 그 외 경로에서는 기존 파싱 규칙 그대로다.
  const parsedApplication = getApplicationTypeAndNameFromPath(props.pathname);
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
