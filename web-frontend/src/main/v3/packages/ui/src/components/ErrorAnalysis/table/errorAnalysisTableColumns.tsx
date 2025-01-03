import { formatDistanceToNowStrict, format } from 'date-fns';
import { LuArrowUp, LuArrowDown } from 'react-icons/lu';
import { RxClock } from 'react-icons/rx';
import { ColumnDef } from '@tanstack/react-table';
import { ErrorAnalysisErrorList } from '@pinpoint-fe/ui/constants';
import {
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '../../ui';
import { LuChevronsUpDown } from 'react-icons/lu';
import { RxArrowUp, RxArrowDown } from 'react-icons/rx';
import { cn } from '../../../lib';

interface ErrorTableColumnProps {
  orderBy?: string;
  isDesc?: boolean;
  onClickColumnHeader: (accessorKey: string, order?: boolean) => void;
}

const DESCENDING = 'desc';
const ASCENDING = 'asc';
const summurySortListMap = {
  errorClassName_asc: 'Error ClassName Asc',
  errorClassName_desc: 'Error ClassName Desc',
  path_asc: 'Path Asc',
  path_desc: 'Path Desc',
};

const ColumnHeaderButton = ({
  accessorKey,
  title,
  orderBy,
  isDesc,
  className,
  onClickColumnHeader,
}: {
  accessorKey: string;
  title: React.ReactNode;
  className?: string;
} & ErrorTableColumnProps) => {
  return (
    <Button
      className={cn('w-full justify-start', className)}
      variant="ghost"
      onClick={() => onClickColumnHeader(accessorKey)}
    >
      {title} {orderBy === accessorKey ? isDesc ? <LuArrowDown /> : <LuArrowUp /> : ''}
    </Button>
  );
};

export const errorTableColumns = ({
  orderBy,
  isDesc,
  onClickColumnHeader,
}: ErrorTableColumnProps): ColumnDef<ErrorAnalysisErrorList.ErrorData>[] => [
  {
    accessorKey: 'errorMessage',
    header: () => {
      const currentOrderBy =
        summurySortListMap[
          `${orderBy}_${isDesc ? DESCENDING : ASCENDING}` as keyof typeof summurySortListMap
        ];

      return (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button className="w-full px-1.5" variant="ghost">
              Summary
              <div className="flex ml-auto text-xs items-center gap-1.5">
                {currentOrderBy && (
                  <div className="flex gap-1.5 items-center">
                    {isDesc ? <RxArrowDown /> : <RxArrowUp />}
                    {currentOrderBy}
                  </div>
                )}
                <LuChevronsUpDown />
              </div>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuLabel className="text-xs">Order by</DropdownMenuLabel>
            <DropdownMenuSeparator />
            {Object.keys(summurySortListMap).map((key, i) => {
              const splittedValue = key.split('_');
              const orderKey = splittedValue[0];
              const order = splittedValue[1] === DESCENDING ? true : false;
              const text = summurySortListMap[key as keyof typeof summurySortListMap];

              return (
                <DropdownMenuItem
                  key={i}
                  onClick={() => {
                    onClickColumnHeader?.(orderKey, order);
                  }}
                >
                  <div className="flex gap-1.5 items-center">
                    {order ? <RxArrowDown /> : <RxArrowUp />}
                    {text}
                  </div>
                </DropdownMenuItem>
              );
            })}
          </DropdownMenuContent>
        </DropdownMenu>
      );
    },
    cell: (props) => {
      const original = props.row.original;
      const code = props.getValue() as string;

      return (
        <>
          <div className="flex items-center mb-2 space-x-1 break-all">
            <div className="w-1 h-4 rounded-sm min-w-1 bg-status-fail" />
            <div className="text-sm font-semibold line-clamp-1">{original.errorClassName}</div>
            <div className="text-xs line-clamp-1">{original.uriTemplate}</div>
          </div>
          <div className="h-12 p-2 overflow-hidden hljs text-muted-foreground">
            <code className="break-all line-clamp-2">{code}</code>
          </div>
        </>
      );
    },
    meta: {
      headerClassName: 'px-1.5',
    },
  },
  {
    accessorKey: 'timestamp',
    header: () => (
      <ColumnHeaderButton
        className="justify-end gap-1 pr-2 text-right"
        accessorKey="timestamp"
        title={<RxClock />}
        onClickColumnHeader={onClickColumnHeader}
        {...{ orderBy, isDesc }}
      />
    ),
    cell: (props) => {
      const timestamp = props.getValue() as number;

      return (
        <Tooltip>
          <TooltipTrigger>
            <span className="text-muted-foreground">{formatDistanceToNowStrict(timestamp)}</span>
          </TooltipTrigger>
          <TooltipContent>{format(timestamp, 'yyyy MM.dd hh:mm:ss')}</TooltipContent>
        </Tooltip>
      );
    },
    meta: {
      headerClassName: 'w-20',
      cellClassName: 'text-right pr-4',
    },
  },
];
