import { Transaction } from '@pinpoint-fe/ui/src/constants';
import { VirtualizedDataTable, VirtualizedDataTableProps } from '../../DataTable';
import { transactionListTableColumns } from '.';
import { useTimezone, useTransactionSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import {
  convertParamsToQueryString,
  getTransactionListPath,
  getTransactionTableUniqueKey,
} from '@pinpoint-fe/ui/src/utils';
import { useNavigate } from 'react-router-dom';
import { useSetAtom } from 'jotai';
import { transactionInfoCallTreeFocusId } from '@pinpoint-fe/ui/src/atoms';

export interface TransactionListTableProps
  extends Pick<VirtualizedDataTableProps<Transaction, unknown>, 'onClickRow'> {
  data?: Transaction[];
}

export const TransactionListTable = ({ data, ...props }: TransactionListTableProps) => {
  const navigate = useNavigate();
  const { transactionInfo, searchParameters, application } = useTransactionSearchParameters();
  const [timezone] = useTimezone();
  const setCallTreeFocusId = useSetAtom(transactionInfoCallTreeFocusId);

  const columns = transactionListTableColumns(application, timezone);

  return (
    <VirtualizedDataTable
      enableSorting
      enableColumnResizing
      tableClassName="text-xs [&>thead>tr]:hover:bg-background [&>thead>tr]:bg-background [&_td]:p-1.5"
      rowClassName={(row) => {
        if (
          getTransactionTableUniqueKey(row.original) ===
          getTransactionTableUniqueKey(transactionInfo)
        ) {
          return row.original.exception ? 'bg-rose-100' : 'bg-muted';
        }
        return row.original.exception ? 'bg-rose-50' : '';
      }}
      data={data || []}
      scrollToIndex={(rows) =>
        rows.findIndex(
          (r) =>
            getTransactionTableUniqueKey(r.original) ===
            getTransactionTableUniqueKey(transactionInfo),
        )
      }
      columns={columns}
      columnSorting={[{ id: 'elapsed', desc: true }]}
      onClickRow={(row) => {
        setCallTreeFocusId('');

        const rowData = row.original;
        navigate(
          `${getTransactionListPath(application)}?${convertParamsToQueryString({
            from: searchParameters.from,
            to: searchParameters.to,
            dragInfo: searchParameters.dragInfo,
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
      // enableMultiRowSelection={enableMultiRowSelection}
      // onChangeRowSelection={(datas) => {
      // setSqlSelectedSummaryDatas(datas);
      // }}
    />
  );
};
