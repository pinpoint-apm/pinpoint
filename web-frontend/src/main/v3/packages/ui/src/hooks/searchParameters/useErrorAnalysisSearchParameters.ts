import { useLocation } from 'react-router-dom';
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
  const transactionInfo = decodeURIComponent(searchParameters?.transactionInfo || '');
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
