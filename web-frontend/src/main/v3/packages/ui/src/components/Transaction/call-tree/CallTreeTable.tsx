import { TransactionInfoType as TransactionInfo } from '@pinpoint-fe/ui/src/constants';
import { VirtualizedDataTable, VirtualizedDataTableProps } from '../../DataTable';
import { getCallTreeRowClasses } from './rowClasses';

export interface CallTreeTableProps extends VirtualizedDataTableProps<
  TransactionInfo.CallStackKeyValueMap,
  unknown
> {
  data: TransactionInfo.CallStackKeyValueMap[];
  metaData: TransactionInfo.Response;
  filteredRowIds?: string[];
  // Row to highlight. Kept separate from `focusRowIndex` (which only scrolls) because the two
  // diverge as soon as the tree is re-rooted by "Step in".
  highlightRowId?: string;
}

export const CallTreeTable = ({
  columns,
  metaData,
  data,
  filteredRowIds,
  highlightRowId,
  ...props
}: CallTreeTableProps) => {
  return (
    <VirtualizedDataTable
      enableColumnResizing
      tableClassName="text-xs [&_td]:p-1.5"
      rowClassName={(row) =>
        getCallTreeRowClasses(row.original, { filteredRowIds, highlightRowId })
      }
      data={data || []}
      columns={columns || []}
      {...props}
    />
  );
};
