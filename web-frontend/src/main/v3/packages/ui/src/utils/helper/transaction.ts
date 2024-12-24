import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/constants';
import { getTransactionDetailPath } from './route';

export type TransactionListQueryParam = {
  x1: number;
  x2: number;
  y1: number;
  y2: number;
  checkedLegends: string[];
  agentId?: string;
};

export const getTranscationListQueryString = (queryParam: TransactionListQueryParam) => {
  return `dragInfo=${encodeURI(
    JSON.stringify({
      x1: Math.floor(queryParam.x1),
      x2: Math.ceil(queryParam.x2),
      y1: queryParam.y2 <= 0 ? 0 : Math.floor(queryParam.y2),
      y2: Math.ceil(queryParam.y1),
      agentId: queryParam.agentId || '',
      dotStatus: queryParam.checkedLegends,
    }),
  )}`;
};

export const getTransactionDetailQueryString = (queryParam: TransactionInfo.Parameters) => {
  return `transactionInfo=${encodeURI(
    JSON.stringify({
      agentId: queryParam.agentId,
      spanId: queryParam.spanId,
      traceId: queryParam.traceId,
      focusTimestamp: queryParam.focusTimestamp,
    }),
  )}`;
};

export const getTransactionTableUniqueKey = (transaction: {
  traceId?: string;
  spanId?: string;
  application?: string;
  path?: string;
}) => {
  return `${transaction?.traceId}${transaction?.spanId}${
    transaction?.application || transaction?.path
  }`;
};

export const getTransactionDetailPathByTransactionId = (transactionId: string) => {
  const trimmedId = transactionId.trim();
  const [agentId] = trimmedId.split('^');

  return `${getTransactionDetailPath()}?${getTransactionDetailQueryString({
    agentId,
    spanId: '-1',
    traceId: trimmedId,
    focusTimestamp: 0,
  })}`;
};
