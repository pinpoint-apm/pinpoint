import useSWR from 'swr';
import { END_POINTS, TransactionInfo } from '@pinpoint-fe/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/utils';
import { useTransactionSearchParameters } from '../searchParameters';
import { swrConfigs } from './swrConfigs';
import { useExperimentals } from '../utility';

const getQueryString = (queryParams: Partial<TransactionInfo.Parameters>) => {
  if (queryParams?.agentId && queryParams?.spanId && queryParams?.traceId) {
    return '?' + convertParamsToQueryString(queryParams);
  }
  return '';
};

export const useGetTransactionInfo = () => {
  const { statisticsAgentState } = useExperimentals();
  const { transactionInfo } = useTransactionSearchParameters();

  const queryParams = {
    agentId: transactionInfo?.agentId,
    spanId: transactionInfo?.spanId,
    traceId: transactionInfo?.traceId,
    focusTimestamp: transactionInfo?.focusTimestamp,
    useStatisticsAgentState: statisticsAgentState.value,
  };

  const queryString = getQueryString(queryParams);

  const { data, isLoading, isValidating } = useSWR<TransactionInfo.Response>(
    queryString ? `${END_POINTS.TRANSACTION_INFO}${queryString}` : null,
    swrConfigs,
  );
  const mapData = getMapData(data);
  const tableData = convertToTree(mapData, '');

  return { data, tableData, isLoading, isValidating, mapData };
};

const getMapData = (data?: TransactionInfo.Response) => {
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
  parentId?: string,
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
