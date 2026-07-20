import { useNavigate } from 'react-router-dom';
import { useSetAtom } from 'jotai';
import { TransactionListTable, TransactionListTableProps } from './TransactionListTable';
import {
  useGetTransactionTraceMetadata,
  useTransactionSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { convertParamsToQueryString, getTransactionListPath } from '@pinpoint-fe/ui/src/utils';
import { transactionInfoCallTreeFocusId } from '@pinpoint-fe/ui/src/atoms';

export interface TransactionListByTraceProps extends TransactionListTableProps {}

/**
 * List panel source for traceId-lookup mode (?traceInfo=...), entered by following
 * an OTel Link from the Call Tree. Rows come from a /api/transaction/metadata lookup
 * instead of heatmap drag / filter map, so row clicks must preserve traceInfo
 * (the default TransactionListTable handler only carries dragInfo).
 */
export const TransactionListByTrace = (props: TransactionListByTraceProps) => {
  const navigate = useNavigate();
  const { application, searchParameters, traceInfo } = useTransactionSearchParameters();
  const setCallTreeFocusId = useSetAtom(transactionInfoCallTreeFocusId);
  const { data } = useGetTransactionTraceMetadata({ traceId: traceInfo });

  return (
    <TransactionListTable
      data={data?.metadata}
      onClickRow={(row) => {
        setCallTreeFocusId('');

        const rowData = row.original;
        navigate(
          `${getTransactionListPath(application)}?${convertParamsToQueryString({
            from: searchParameters.from,
            to: searchParameters.to,
            traceInfo,
            transactionInfo: JSON.stringify({
              agentId: rowData.agentId,
              spanId: rowData.spanId,
              traceId: rowData.traceId,
              focusTimestamp: rowData.collectorAcceptTime,
              path: rowData.application,
            }),
          })}`,
        );
      }}
      {...props}
    />
  );
};
