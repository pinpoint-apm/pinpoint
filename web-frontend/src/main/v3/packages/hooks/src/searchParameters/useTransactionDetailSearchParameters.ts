import { useSearchParameters } from './useSearchParameters';

export const useTransactionDetailSearchParameters = () => {
  const props = useSearchParameters();
  const parsedTransactionInfo = props.searchParameters?.transactionInfo
    ? JSON.parse(props.searchParameters?.transactionInfo)
    : null;
  return {
    ...props,
    transactionInfo: parsedTransactionInfo,
  };
};
