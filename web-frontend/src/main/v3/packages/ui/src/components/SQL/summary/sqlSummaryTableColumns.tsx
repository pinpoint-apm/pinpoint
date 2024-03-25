import { LuArrowUp, LuArrowDown } from 'react-icons/lu';
import { ColumnDef } from '@tanstack/react-table';
import { SqlStatSummary } from '@pinpoint-fe/constants';
import { HighLightCode } from '../../HighLightCode/HighLightCode';
import { Button, Checkbox } from '../../ui';
import { addCommas, numberInDecimal } from '@pinpoint-fe/utils';

interface SummaryColumnProps {
  orderBy: string;
  isDesc: boolean;
  groupBy?: string;
  enableMultiRowSelection?: boolean;
  onClickColumnHeader: (accessorKey: string) => void;
}

const ColumnHeaderButton = ({
  accessorKey,
  title,
  orderBy,
  isDesc,
  onClickColumnHeader,
}: {
  accessorKey: string;
  title: string;
} & SummaryColumnProps) => {
  return (
    <Button
      className="justify-end w-full gap-1 pr-2 text-right"
      variant="ghost"
      onClick={() => onClickColumnHeader(accessorKey)}
    >
      {title} {orderBy === accessorKey ? isDesc ? <LuArrowDown /> : <LuArrowUp /> : ''}
    </Button>
  );
};

export const summaryColumns = ({
  orderBy,
  isDesc,
  groupBy,
  enableMultiRowSelection,
  onClickColumnHeader,
}: SummaryColumnProps): ColumnDef<SqlStatSummary.SummaryData>[] => {
  const checkColumn: ColumnDef<SqlStatSummary.SummaryData> = {
    id: 'select',
    header: ({ table }) => (
      <Checkbox
        checked={table.getIsAllPageRowsSelected()}
        onCheckedChange={(value) => table.toggleAllPageRowsSelected(!!value)}
        aria-label="Select all"
        className="border-border data-[state=checked]:bg-transparent data-[state=checked]:text-primary"
      />
    ),
    cell: ({ row }) => (
      <Checkbox
        checked={row.getIsSelected()}
        onClick={(event) => event.stopPropagation()}
        onCheckedChange={(value) => row.toggleSelected(!!value)}
        aria-label="Select row"
        className="border-border data-[state=checked]:bg-transparent data-[state=checked]:text-primary"
      />
    ),
    meta: {
      headerClassName: 'w-6',
      cellClassName: '[&>[role=checkbox]]:transform-none',
    },
  };

  const columns: ColumnDef<SqlStatSummary.SummaryData>[] = [
    {
      accessorKey: 'label',
      header: () => {
        return <div className="first-letter:uppercase">{groupBy ? groupBy : 'Query'}</div>;
      },
      cell: (props) => {
        const code = props.getValue() as string;

        return <HighLightCode language="sql" code={code} className="p-2 text-xs" />;
      },
    },

    {
      accessorKey: 'avgTime',
      header: () => (
        <ColumnHeaderButton
          accessorKey="avgTime"
          title="Avg(ms)"
          onClickColumnHeader={onClickColumnHeader}
          {...{ orderBy, isDesc }}
        />
      ),
      cell: (props) => {
        const avg = props.getValue() as number;
        return numberInDecimal(avg, 2);
      },
      meta: {
        headerClassName: 'w-24',
        cellClassName: 'px-4 text-right',
      },
    },
    {
      accessorKey: 'totalTime',
      header: () => (
        <ColumnHeaderButton
          accessorKey="totalTime"
          title="Total Time(ms)"
          onClickColumnHeader={onClickColumnHeader}
          {...{ orderBy, isDesc }}
        />
      ),
      cell: (props) => {
        const avg = props.getValue() as number;
        return addCommas(avg);
      },
      meta: {
        headerClassName: 'w-40',
        cellClassName: 'px-4 text-right',
      },
    },
    {
      accessorKey: 'totalCount',
      header: () => (
        <ColumnHeaderButton
          accessorKey="totalCount"
          title="Total Count"
          onClickColumnHeader={onClickColumnHeader}
          {...{ orderBy, isDesc }}
        />
      ),
      cell: (props) => {
        const avg = props.getValue() as number;
        return addCommas(avg);
      },
      meta: {
        headerClassName: 'w-36',
        cellClassName: 'px-4 text-right',
      },
    },
  ];

  if (enableMultiRowSelection) {
    return [checkColumn, ...columns];
  }
  return columns;
};
