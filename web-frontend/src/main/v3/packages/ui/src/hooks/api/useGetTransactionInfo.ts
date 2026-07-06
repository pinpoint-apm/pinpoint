import { useSuspenseQuery } from '@tanstack/react-query';
import { END_POINTS, TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { convertParamsToQueryString } from '@pinpoint-fe/ui/src/utils';
import { useTransactionSearchParameters } from '../searchParameters';
import { queryFn } from './reactQueryHelper';

const hasLinkParams = (queryParams: Partial<TransactionInfo.Parameters>) =>
  !!queryParams.linkTraceId && !!queryParams.linkSpanId;

const getQueryString = (queryParams: Partial<TransactionInfo.Parameters>) => {
  if (hasLinkParams(queryParams)) {
    if (queryParams.spanId && queryParams.traceId) {
      return '?' + convertParamsToQueryString(queryParams);
    }
    return '';
  }
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
    linkTraceId: transactionInfo?.linkTraceId,
    linkSpanId: transactionInfo?.linkSpanId,
  };

  const queryString = getQueryString(queryParams);
  const endpoint = hasLinkParams(queryParams)
    ? END_POINTS.TRANSACTION_INFO_LINK
    : END_POINTS.TRANSACTION_INFO;

  const { data, isLoading, isFetching } = useSuspenseQuery<TransactionInfo.Response | null>({
    queryKey: [endpoint, queryString],
    queryFn: queryString ? queryFn(`${endpoint}${queryString}`) : async () => null,
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
  // Index nodes by parentId once so both child and Attribute lookups are O(1) during
  // recursion. A naive scan (items.find / re-filtering items per node) is O(n^2) and
  // gets slow for large call stacks.
  const childrenByParentId = new Map<unknown, TransactionInfo.CallStackKeyValueMap[]>();
  for (const item of items) {
    const siblings = childrenByParentId.get(item.parentId);
    if (siblings) {
      siblings.push(item);
    } else {
      childrenByParentId.set(item.parentId, [item]);
    }
  }

  const build = (pId: unknown): TransactionInfo.CallStackKeyValueMap[] => {
    const result: TransactionInfo.CallStackKeyValueMap[] = [];

    for (const item of childrenByParentId.get(pId) ?? []) {
      // The backend emits a separate "Attribute" child row per node. Lift its JSON
      // onto the parent method row (rendered as an icon) and drop the standalone row.
      if (item.title === 'Attribute') {
        continue;
      }

      const newItem: TransactionInfo.CallStackKeyValueMap = {
        ...item,
      };

      const attributeChild = (childrenByParentId.get(item.id) ?? []).find(
        (i) => i.title === 'Attribute',
      );
      if (attributeChild) {
        newItem.attributes = attributeChild.arguments;
      }

      const subRows = build(item.id);
      if (subRows.length > 0) {
        newItem.subRows = subRows;
      }

      result.push(newItem);
    }

    return result;
  };

  return build(parentId);
};

const getAgentKey = (datas: TransactionInfo.Response, rowIndex: number) => {
  let agentKey = null;

  for (let i = rowIndex - 1; agentKey === null; i--) {
    agentKey = datas.callStack[i][datas?.callStackIndex?.agent];
  }
  return agentKey;
};
