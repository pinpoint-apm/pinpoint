import { useSearchParameters } from './useSearchParameters';
import { getDateRange } from './utils';

export const useTransactionSearchParameters = () => {
  const props = useSearchParameters();
  const dateRange = getDateRange(props.search, false);
  const parseDragInfo = props.searchParameters?.dragInfo
    ? JSON.parse(props.searchParameters.dragInfo)
    : null;
  const parsedTransactionInfo = props.searchParameters?.transactionInfo
    ? JSON.parse(props.searchParameters?.transactionInfo)
    : null;
  return {
    ...props,
    dateRange,
    dragInfo: parseDragInfo,
    transactionInfo: parsedTransactionInfo,
  };
};
