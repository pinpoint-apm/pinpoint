import { useGetActiveThreadLightDump } from '@pinpoint-fe/ui/hooks';
import { transactionListTableColumns } from './threadDumpColumns';
import { VirtualizedDataTable } from '../../components/DataTable';
import { ActiveThreadLightDump } from '@pinpoint-fe/ui/constants';
import { Row } from '@tanstack/react-table';

export interface ThreadDumpListFectherProps {
  selectedThread?: ActiveThreadLightDump.ThreadDumpData;
  onClickRow?: (row: Row<ActiveThreadLightDump.ThreadDumpData>) => void;
}

export const ThreadDumpListFecther = ({
  selectedThread,
  onClickRow,
}: ThreadDumpListFectherProps) => {
  const { data } = useGetActiveThreadLightDump();
  const columns = transactionListTableColumns();

  return (
    <div className="h-full border rounded-md">
      <VirtualizedDataTable
        enableColumnResizing
        tableClassName="text-xs [&_td]:p-1.5"
        data={data?.message.threadDumpData || []}
        columns={columns}
        rowClassName={(row) => {
          return row.original.localTraceId === selectedThread?.localTraceId ? 'bg-muted' : '';
        }}
        onClickRow={(rowData) => {
          onClickRow?.(rowData);
        }}
      />
    </div>
  );
};
