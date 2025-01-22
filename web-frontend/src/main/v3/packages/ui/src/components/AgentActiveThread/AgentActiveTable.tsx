import { VirtualizedDataTable } from '../DataTable';
import { ColumnDef } from '@tanstack/react-table';
import { cn } from '@pinpoint-fe/ui/lib';
import React from 'react';
import { TooltipContent, TooltipProvider, Tooltip, TooltipTrigger, Button } from '../ui';
import * as TooltipPrimitive from '@radix-ui/react-tooltip';
import { HiMiniExclamationCircle } from 'react-icons/hi2';
import { BASE_PATH, colors } from '@pinpoint-fe/ui/constants';
import { RxExternalLink } from 'react-icons/rx';
import { getThreadDumpPath } from '@pinpoint-fe/ui/utils';
import { useSearchParameters } from '@pinpoint-fe/ui/hooks';

export type AgentActiveData = {
  server: string;
  '1s': number;
  '3s': number;
  '5s': number;
  slow: number;
  message?: string;
};

const SIZE = 50;

export const AgentActiveTable = ({
  loading,
  data,
  clickedActiveThread,
}: {
  loading?: boolean;
  data: AgentActiveData[];
  clickedActiveThread?: string;
}) => {
  const { application } = useSearchParameters();
  const focusRowId = React.useMemo(() => {
    return data.findIndex((d) => d.server === clickedActiveThread);
  }, [data, clickedActiveThread]);

  const columns: ColumnDef<AgentActiveData>[] = [
    {
      accessorKey: 'server',
      header: 'Server',
      size: 230,
      cell: ({ getValue, row }) => {
        const value = getValue<string>() || '';
        const message = row?.original?.message || '';

        return (
          <div className="flex items-center gap-1">
            <span>{value}</span>
            <Button
              className="text-muted-foreground p-0 w-4 h-4 mr-1.5"
              variant="ghost"
              onClick={() => {
                window.open(`${BASE_PATH}${getThreadDumpPath(application)}?agentId=${value}`);
              }}
            >
              <RxExternalLink />
            </Button>
            {true && (
              <TooltipProvider delayDuration={0}>
                <Tooltip>
                  <TooltipTrigger asChild>
                    <div>
                      <HiMiniExclamationCircle color={colors.red[500]} size={16} />
                    </div>
                  </TooltipTrigger>
                  <TooltipPrimitive.Portal>
                    <TooltipContent>
                      <p>{message}</p>
                    </TooltipContent>
                  </TooltipPrimitive.Portal>
                </Tooltip>
              </TooltipProvider>
            )}
          </div>
        );
      },
    },
    {
      accessorKey: 'slow',
      header: 'Slow',
      size: SIZE,
      meta: {
        headerClassName: 'flex justify-end',
        cellClassName: 'text-right',
      },
      cell: ({ getValue }) => {
        const value = getValue<number>() || 0;

        if (value === -1) {
          return '-';
        }

        return (
          <span
            className={cn({
              'text-red-500 font-bold': value > 0,
            })}
          >
            {value}
          </span>
        );
      },
    },
    {
      accessorKey: '5s',
      header: '5s',
      size: SIZE,
      meta: {
        headerClassName: 'flex justify-end',
        cellClassName: 'text-right',
      },
      cell: ({ getValue }) => {
        const value = getValue<number>() || 0;
        if (value === -1) {
          return '-';
        }
        return (
          <span
            className={cn({
              'text-red-500 font-bold': value > 0,
            })}
          >
            {value}
          </span>
        );
      },
    },
    {
      accessorKey: '3s',
      header: '3s',
      size: SIZE,
      meta: {
        headerClassName: 'flex justify-end',
        cellClassName: 'text-right',
      },
      cell: ({ getValue }) => {
        const value = getValue<number>() || 0;
        if (value === -1) {
          return '-';
        }
        return value;
      },
    },
    {
      accessorKey: '1s',
      header: '1s',
      size: SIZE,
      meta: {
        headerClassName: 'flex justify-end',
        cellClassName: 'text-right',
      },
      cell: ({ getValue }) => {
        const value = getValue<number>() || 0;
        if (value === -1) {
          return '-';
        }
        return value;
      },
    },
  ];

  return (
    <div className="block h-[-webkit-fill-available] max-w-[50%] w-auto">
      <VirtualizedDataTable
        loading={loading}
        tableClassName="[&>tbody]:text-xs w-auto"
        columns={columns}
        data={loading ? [] : data || []}
        focusRowIndex={focusRowId}
        rowClassName={(row) => {
          if (row?.id === String(focusRowId)) {
            return 'bg-yellow-200';
          }
          return '';
        }}
      />
    </div>
  );
};
