import { VirtualizedDataTable } from '../DataTable';
import { ColumnDef } from '@tanstack/react-table';
import { cn } from '@pinpoint-fe/ui/lib';
import React from 'react';

export type AgentActiveData = {
  server: string;
  '1s': number;
  '3s': number;
  '5s': number;
  slow: number;
};

const SIZE = 50;

export const AgentActiveTable = ({
  data,
  clickedActiveThread,
}: {
  data: AgentActiveData[];
  clickedActiveThread?: string;
}) => {
  const focusRowId = React.useMemo(() => {
    return data.findIndex((d) => d.server === clickedActiveThread);
  }, [data, clickedActiveThread]);

  const columns: ColumnDef<AgentActiveData>[] = [
    {
      accessorKey: 'server',
      header: 'Server',
      size: 200,
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
    },
    {
      accessorKey: '1s',
      header: '1s',
      size: SIZE,
      meta: {
        headerClassName: 'flex justify-end',
        cellClassName: 'text-right',
      },
    },
  ];

  return (
    <div className="block h-[-webkit-fill-available] max-w-[50%] w-auto">
      <VirtualizedDataTable
        tableClassName="[&>tbody]:text-xs w-auto"
        columns={columns}
        data={data || []}
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
