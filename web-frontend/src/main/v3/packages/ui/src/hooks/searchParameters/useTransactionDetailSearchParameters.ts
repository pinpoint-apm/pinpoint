import { useSearchParameters } from './useSearchParameters';

export const useTransactionDetailSearchParameters = () => {
  const props = useSearchParameters();
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
    transactionInfo: parsedTransactionInfo,
  };
};
