import { useSearchParameters } from './useSearchParameters';
import { getDateRange } from './utils';

export const useTransactionSearchParameters = () => {
  const props = useSearchParameters();
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
  return {
    ...props,
    dateRange,
    withFilter,
    dragInfo: parseDragInfo,
    transactionInfo: parsedTransactionInfo,
  };
};
