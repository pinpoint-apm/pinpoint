import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useTransactionSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

const getQueryString = (queryParams: Partial<TransactionInfo.Parameters>) => {
  if (queryParams?.agentId && queryParams?.spanId && queryParams?.traceId) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetTransactionInfo = () => {
  const { transactionInfo } = useTransactionSearchParameters();

  const queryParams: Partial<TransactionInfo.Parameters> = {
    agentId: transactionInfo?.agentId,
    spanId: transactionInfo?.spanId,
    traceId: transactionInfo?.traceId,
    focusTimestamp: transactionInfo?.focusTimestamp,
  };

  const queryString = getQueryString(queryParams);

  const { data, isLoading, isFetching } = useSuspenseQuery<TransactionInfo.Response | null>({
    queryKey: [END_POINTS.TRANSACTION_INFO, queryString],
    queryFn: queryString
      ? queryFn(`${END_POINTS.TRANSACTION_INFO}${queryString}`)
      : async () => null,
  });
  const mapData = getMapData(data);
  const tableData = convertToTree(mapData, null);

  return { data, tableData, isLoading, isValidating: isFetching, mapData };
};

const getMapData = (data?: TransactionInfo.Response | null) => {
  return data?.callStack.map((callStack, i) => {
    return Object.entries(data?.callStackIndex).reduce((acc, curr) => {
      if (curr[0] === 'agent' && !callStack[curr[1]]) {
        return {
          ...acc,
          [curr[0]]: callStack[curr[1]],
          attributedAgent: getAgentKey(data, i),
        };
      }
      return {
        ...acc,
        [curr[0]]: callStack[curr[1]],
      };
    }, {} as TransactionInfo.CallStackKeyValueMap);
  });
};

const convertToTree = (
  items: TransactionInfo.CallStackKeyValueMap[] = [],
  parentId?: string | null,
): TransactionInfo.CallStackKeyValueMap[] => {
  const result: TransactionInfo.CallStackKeyValueMap[] = [];

  for (const item of items) {
    if (item.parentId === parentId) {
      const newItem: TransactionInfo.CallStackKeyValueMap = {
        ...item,
      };

      const subRows = convertToTree(items, item.id);
      if (subRows.length > 0) {
        newItem.subRows = subRows;
      }

      result.push(newItem);
    }
  }

  return result;
};

const getAgentKey = (datas: TransactionInfo.Response, rowIndex: number) => {
  let agentKey = null;

  for (let i = rowIndex - 1; agentKey === null; i--) {
    agentKey = datas.callStack[i][20]; // 20th index indicates agentKey
  }
  return agentKey;
};
