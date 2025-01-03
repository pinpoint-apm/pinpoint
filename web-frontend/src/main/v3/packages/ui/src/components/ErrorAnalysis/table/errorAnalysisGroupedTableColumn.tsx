import { ColumnDef } from '@tanstack/react-table';
import { RxCross2 } from 'react-icons/rx';
import { Badge } from '../../ui';
import { ErrorAnalysisGroupedErrorList } from '@pinpoint-fe/ui/constants';
import { addCommas, format } from '@pinpoint-fe/ui/utils';
import { ErrorGroupedTableVolumneChart } from './ErrorGroupedTableVolumneChart';
// import { cn } from '../../../lib';

interface ErrorGroupedTableColumnProps {
  groupBy?: string[];
  onClickGroupBy?: (group: string) => void;
}

export const errorGroupedTableColumns = ({
  groupBy,
  onClickGroupBy,
}: ErrorGroupedTableColumnProps): ColumnDef<ErrorAnalysisGroupedErrorList.ErrorData>[] => [
  {
    accessorKey: 'mostRecentErrorClass',
    header: () => {
      return (
        <div className="flex flex-wrap items-center gap-1">
          <span className="mr-3">Group by</span>
          {groupBy?.map((group, i) => {
            return (
              <Badge
                className="gap-1 border cursor-pointer border-muted-foreground/40 bg-secondary"
                onClick={() => onClickGroupBy?.(group)}
                variant="secondary"
                key={i}
              >
                {group}
                <RxCross2 />
              </Badge>
            );
          })}
        </div>
      );
    },
    cell: (props) => {
      const original = props.row.original;
      const fieldName = original?.groupedFieldName;

      return (
        <>
          {fieldName?.stackTraceHash && (
            <div className="mb-1 text-xxs">{fieldName.stackTraceHash}</div>
          )}
          <div className="flex items-center mb-2 space-x-1 break-all">
            <div className="w-1 h-4 rounded-sm min-w-1 bg-status-fail" />
            <div className="text-sm font-semibold line-clamp-1">
              {fieldName?.errorClassName || original.mostRecentErrorClass}
            </div>
            <div className="text-xs line-clamp-1">{fieldName?.uriTemplate}</div>
          </div>
          <div className="h-12 p-2 overflow-hidden hljs text-muted-foreground">
            <code className="break-all line-clamp-2">
              {fieldName?.errorMessage || original.mostRecentErrorMessage}
            </code>
          </div>
        </>
      );
    },
  },
  {
    accessorKey: 'firstOccurred',
    header: 'First Occured',
    cell: (props) => {
      const timestamp = props.getValue() as number;
      return format(timestamp, 'MMM do HH:mm');
    },
    meta: {
      headerClassName: 'w-32',
    },
  },
  {
    accessorKey: 'lastOccurred',
    header: 'Last Occured',
    cell: (props) => {
      const timestamp = props.getValue() as number;
      return format(timestamp, 'MMM do HH:mm');
    },
    meta: {
      headerClassName: 'w-32',
    },
  },
  {
    accessorKey: 'chart',
    header: 'Volume',
    cell: (props) => {
      const chart = props.getValue() as ErrorAnalysisGroupedErrorList.ErrorData['chart'];
      return <ErrorGroupedTableVolumneChart chart={chart} />;
    },
  },
  {
    accessorKey: 'count',
    header: 'Counts',
    cell: (props) => {
      const count = props.getValue() as number;
      return addCommas(count);
    },
    meta: {
      headerClassName: 'text-right pr-4',
      cellClassName: 'text-right pr-4',
    },
  },
];
