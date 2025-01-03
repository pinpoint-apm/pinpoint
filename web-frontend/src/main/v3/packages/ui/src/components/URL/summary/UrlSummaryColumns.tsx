import { LuArrowUp, LuArrowDown } from 'react-icons/lu';
import { ColumnDef } from '@tanstack/react-table';
import { UrlStatSummary } from '@pinpoint-fe/ui/constants';
import { Button } from '../../ui';
import { addCommas, numberInDecimal, numberInInteger } from '@pinpoint-fe/ui/utils';
import { cn } from '../../../lib';

interface SummaryColumnProps {
  orderBy: string;
  isDesc: boolean;
  onClickColumnHeader: (accessorKey: string) => void;
}

const ColumnHeaderButton = ({
  accessorKey,
  title,
  orderBy,
  isDesc,
  className,
  onClickColumnHeader,
}: {
  accessorKey: string;
  title: string;
  className?: string;
} & SummaryColumnProps) => {
  return (
    <Button
      className={cn('pr-2 w-full text-right justify-end gap-1', className)}
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
  onClickColumnHeader,
}: SummaryColumnProps): ColumnDef<UrlStatSummary.SummaryData>[] => [
  {
    accessorKey: 'uri',
    header: () => (
      <ColumnHeaderButton
        className="justify-start pl-1"
        accessorKey="uri"
        title="Path"
        onClickColumnHeader={onClickColumnHeader}
        {...{ orderBy, isDesc }}
      />
    ),
    cell: (props) => props.getValue(),
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
      const totalCount = props.getValue() as number;
      return addCommas(totalCount);
    },
    meta: {
      headerClassName: 'w-40',
      cellClassName: 'px-4 text-right',
    },
  },
  {
    accessorKey: 'failureCount',
    header: () => (
      <ColumnHeaderButton
        accessorKey="failureCount"
        title="Failure Count"
        onClickColumnHeader={onClickColumnHeader}
        {...{ orderBy, isDesc }}
      />
    ),
    cell: (props) => {
      const failureCount = props.getValue() as number;
      return addCommas(failureCount);
    },
    meta: {
      headerClassName: 'w-40',
      cellClassName: 'px-4 text-right',
    },
  },
  {
    accessorKey: 'apdex',
    header: () => (
      <ColumnHeaderButton
        accessorKey="apdex"
        title="Apdex"
        onClickColumnHeader={onClickColumnHeader}
        {...{ orderBy, isDesc }}
      />
    ),
    cell: (props) => {
      const apdex = props.getValue() as number;
      return numberInDecimal(apdex, 2);
    },
    meta: {
      headerClassName: 'w-24',
      cellClassName: 'px-4 text-right',
    },
  },
  {
    accessorKey: 'avgTimeMs',
    header: () => (
      <ColumnHeaderButton
        accessorKey="avgTimeMs"
        title="Avg(ms)"
        onClickColumnHeader={onClickColumnHeader}
        {...{ orderBy, isDesc }}
      />
    ),
    cell: (props) => {
      const avg = props.getValue() as number;
      return addCommas(numberInInteger(avg));
    },
    meta: {
      headerClassName: 'w-24',
      cellClassName: 'px-4 text-right',
    },
  },
  {
    accessorKey: 'maxTimeMs',
    header: () => (
      <ColumnHeaderButton
        accessorKey="maxTimeMs"
        title="Max(ms)"
        onClickColumnHeader={onClickColumnHeader}
        {...{ orderBy, isDesc }}
      />
    ),
    cell: (props) => {
      const max = props.getValue() as number;
      return addCommas(numberInInteger(max));
    },
    meta: {
      headerClassName: 'w-24',
      cellClassName: 'px-4 text-right',
    },
  },
];
