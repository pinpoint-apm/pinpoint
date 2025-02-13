import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { VirtualizedDataTable, VirtualizedDataTableProps } from '../../DataTable';
import { CallTreeTableColumnsProps, callTreeTableColumns } from '.';

export interface CallTreeTableProps
  extends Omit<
    VirtualizedDataTableProps<TransactionInfo.CallStackKeyValueMap, unknown>,
    'columns'
  > {
  data: TransactionInfo.CallStackKeyValueMap[];
  metaData: TransactionInfo.Response;
  onClickDetailView?: CallTreeTableColumnsProps['onClickDetailView'];
  filteredRowIds?: string[];
}

export const CallTreeTable = ({
  metaData,
  data,
  onClickDetailView,
  filteredRowIds,
  ...props
}: CallTreeTableProps) => {
  const columns = callTreeTableColumns({ metaData, onClickDetailView });
  return (
    <VirtualizedDataTable
      enableColumnResizing
      tableClassName="text-xs [&_td]:p-1.5"
      rowClassName={(row) => {
        const classes = [];

        if (row.original.hasException) {
          classes.push('bg-rose-50');
        }
        if (filteredRowIds?.some((id) => id === row.original.id)) {
          classes.push('bg-yellow-100');
        }
        if (Number(row.original.id) - 1 === props.focusRowIndex) {
          classes.push('bg-yellow-200');
        }

        return classes;
      }}
      data={data || []}
      columns={columns || []}
      {...props}
    />
  );
};
