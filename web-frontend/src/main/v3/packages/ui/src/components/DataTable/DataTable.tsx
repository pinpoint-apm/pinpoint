import React from 'react';
import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  useReactTable,
  Row,
  ColumnFiltersState,
  getFilteredRowModel,
} from '@tanstack/react-table';
import { useUpdateEffect } from 'usehooks-ts';

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../ui/table';
import { cn } from '../../lib';
import { useHeightToBottom } from '@pinpoint-fe/ui/src/hooks';

export type RowFilterInfo = {
  columns?: string[];
  query: string;
};

interface DataTableProps<TData, TValue> {
  data: TData[];
  columns: ColumnDef<TData, TValue>[];
  autoResize?: boolean;
  enableMultiRowSelection?: boolean;
  tableContainerClassName?: string;
  tableClassName?: string;
  rowSelectionInfo?: { [key: number]: boolean };
  rowFilterInfo?: RowFilterInfo;
  emptyMessage?: React.ReactNode;
  onClickRow?: (data: Row<TData>) => void;
  onChangeRowSelection?: (data: TData[]) => void;
}

type MetaType = {
  headerClassName?: string;
  cellClassName?: string;
};

export function DataTable<TData, TValue>({
  data,
  columns,
  autoResize,
  enableMultiRowSelection = false,
  tableClassName,
  rowSelectionInfo = {},
  rowFilterInfo,
  emptyMessage = 'No datas.',
  onClickRow,
  onChangeRowSelection,
}: DataTableProps<TData, TValue>) {
  const containerRef = React.useRef<HTMLDivElement>(null);
  const [rowSelection, setRowSelection] = React.useState<{ [key: number]: boolean }>(
    rowSelectionInfo,
  );
  const [globalFilter, setGlobalFilter] = React.useState('');
  const [columnFilters, setColumnFilters] = React.useState<ColumnFiltersState>([]);
  const table = useReactTable({
    data,
    columns,
    state: {
      rowSelection,
      globalFilter,
      columnFilters,
    },
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    enableRowSelection: true,
    enableMultiRowSelection,
    onRowSelectionChange: setRowSelection,
    onColumnFiltersChange: setColumnFilters,
    onGlobalFilterChange: setGlobalFilter,
  });
  const resizableMaxHeight = useHeightToBottom({ ref: containerRef, disabled: !autoResize });

  React.useEffect(() => {
    setRowSelection(rowSelectionInfo);
  }, [data]);

  useUpdateEffect(() => {
    const rows = table.getSelectedRowModel().rows;
    const selectedRowData = enableMultiRowSelection
      ? rows.map((row) => row.original)
      : rows[0]?.original
        ? [rows[0]?.original]
        : [];

    onChangeRowSelection?.([...selectedRowData]);
  }, [rowSelection]);

  React.useEffect(() => {
    if (rowFilterInfo && Object.keys(rowFilterInfo).length !== 0) {
      const { columns, query } = rowFilterInfo;
      if (columns) {
        columns.forEach((column) => {
          table.getColumn(column)?.setFilterValue(query);
        });
      } else {
        setGlobalFilter(query);
      }
    }
  }, [rowFilterInfo]);

  return (
    <div
      className="relative w-full h-full overflow-auto"
      ref={containerRef}
      style={{ maxHeight: autoResize ? resizableMaxHeight : 'none' }}
    >
      <Table
        className={cn(
          '[&>thead>tr>th]:first:rounded-tl [&>thead>tr>th]:last:rounded-tr',
          tableClassName,
        )}
      >
        <TableHeader>
          {table.getHeaderGroups().map((headerGroup) => (
            <TableRow
              key={headerGroup.id}
              className="sticky top-0 bg-background hover:bg-background"
            >
              {headerGroup.headers.map((header) => {
                return (
                  <TableHead
                    key={header.id}
                    className={cn(
                      'bg-secondary/50',
                      (header.column.columnDef?.meta as MetaType)?.headerClassName,
                    )}
                  >
                    {header.isPlaceholder
                      ? null
                      : flexRender(header.column.columnDef.header, header.getContext())}
                  </TableHead>
                );
              })}
            </TableRow>
          ))}
        </TableHeader>
        <TableBody>
          {table.getRowModel().rows?.length ? (
            table.getRowModel().rows.map((row, i) => (
              <TableRow
                key={row.id}
                data-state={row.getIsSelected() && 'selected'}
                className={cn({ 'cursor-pointer': onClickRow || onChangeRowSelection })}
                onClick={() => {
                  if (onChangeRowSelection) {
                    if (enableMultiRowSelection) {
                      setRowSelection((prev) => ({
                        ...prev,
                        [i]: !prev[i],
                      }));
                    } else {
                      setRowSelection((prev) => ({ [i]: !prev[i] }));
                    }
                  }

                  onClickRow?.(row);
                }}
              >
                {row.getVisibleCells().map((cell) => (
                  <TableCell
                    key={cell.id}
                    className={(cell.column.columnDef?.meta as MetaType)?.cellClassName}
                  >
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </TableCell>
                ))}
              </TableRow>
            ))
          ) : (
            <TableRow>
              <TableCell colSpan={columns.length} className="h-24 text-center">
                {emptyMessage}
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>
  );
}
