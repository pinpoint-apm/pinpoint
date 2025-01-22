import React from 'react';
import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  useReactTable,
  Row,
  getSortedRowModel,
  SortingState,
  ColumnSort,
  ExpandedState,
  getExpandedRowModel,
  Cell,
} from '@tanstack/react-table';
import { LuArrowUp, LuArrowDown } from 'react-icons/lu';
import { useVirtualizer } from '@tanstack/react-virtual';
import { useUpdateEffect } from 'usehooks-ts';

import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '../ui/table';
import { cn } from '../../lib';

type ExpandableTData<T> = T & { subRows?: ExpandableTData<T>[] };

export interface VirtualizedDataTableProps<TData, TValue> {
  loading?: boolean;
  data: TData[];
  columns: ColumnDef<TData, TValue>[];
  focusRowIndex?: number;
  scrollToIndex?: (rows: Row<ExpandableTData<TData>>[]) => number;
  autoResize?: boolean;
  enableSorting?: boolean;
  columnSorting?: ColumnSort[];
  enableColumnResizing?: boolean;
  enableMultiRowSelection?: boolean;
  tableContainerClassName?: string;
  tableClassName?: string;
  rowClassName?: string | ((row: Row<TData>) => string | string[]);
  rowSelectionInfo?: { [key: number]: boolean };
  onClickRow?: (data: Row<TData>) => void;
  onChangeRowSelection?: (data: TData[]) => void;
  onClickCell?: (data: Cell<TData, TValue>) => void;
  onDoubleClickCell?: (data: Cell<TData, TValue>) => void;
}

type MetaType = {
  headerClassName?: string;
  cellClassName?: string;
};

export function VirtualizedDataTable<TData, TValue>({
  loading,
  data,
  columns,
  focusRowIndex,
  scrollToIndex,
  enableSorting = false,
  columnSorting = [],
  enableColumnResizing = false,
  enableMultiRowSelection = false,
  tableClassName,
  rowClassName,
  rowSelectionInfo = {},
  onClickRow,
  onChangeRowSelection,
  onClickCell,
  onDoubleClickCell,
}: VirtualizedDataTableProps<ExpandableTData<TData>, TValue>) {
  const tableContainerRef = React.useRef<HTMLDivElement>(null);
  const [sorting, setSorting] = React.useState<SortingState>(columnSorting);
  const [rowSelection, setRowSelection] = React.useState<{ [key: number]: boolean }>(
    rowSelectionInfo,
  );
  const [expanded, setExpanded] = React.useState<ExpandedState>(true);

  const table = useReactTable({
    data,
    columns,
    defaultColumn: {
      minSize: 10,
    },
    state: {
      rowSelection,
      sorting,
      expanded,
    },
    getCoreRowModel: getCoreRowModel(),
    // resizing
    enableColumnResizing: enableColumnResizing,
    columnResizeMode: 'onChange',
    // sorting
    enableSorting: enableSorting,
    getSortedRowModel: getSortedRowModel(),
    onSortingChange: setSorting,
    // selection
    enableRowSelection: true,
    enableMultiRowSelection,
    onRowSelectionChange: setRowSelection,
    // expand
    getSubRows: (row) => row.subRows,
    onExpandedChange: setExpanded,
    getExpandedRowModel: getExpandedRowModel(),
  });

  const { rows } = table.getRowModel();
  const rowVirtualizer = useVirtualizer({
    count: rows.length,
    estimateSize: () => 29, //estimate row height for accurate scrollbar dragging
    getScrollElement: () => tableContainerRef.current,
    //measure dynamic row height, except in firefox because it measures table border height incorrectly
    measureElement:
      typeof window !== 'undefined' && navigator.userAgent.indexOf('Firefox') === -1
        ? (element) => element?.getBoundingClientRect().height
        : undefined,
    overscan: 5,
  });

  React.useEffect(() => {
    setRowSelection(rowSelectionInfo);
    if (data) {
      const index = scrollToIndex?.(rows) || 0;

      if (index > 0) {
        rowVirtualizer.scrollToIndex(index, { align: 'center' });
      }
    }
  }, [data]);

  React.useEffect(() => {
    const resizeObserver = new ResizeObserver((entries) => {
      entries.forEach(() => {
        table.resetPageSize();
      });
    });
    tableContainerRef.current && resizeObserver.observe(tableContainerRef.current);
    return () => {
      resizeObserver.disconnect();
    };
  }, []);

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
    if (focusRowIndex) {
      rowVirtualizer.scrollToIndex(focusRowIndex, { align: 'center' });
    }
  }, [focusRowIndex]);

  return (
    <div className="relative w-full h-full overflow-auto" ref={tableContainerRef}>
      <Table
        className={cn(tableClassName)}
        style={{
          width:
            table.getCenterTotalSize() > (tableContainerRef.current?.clientWidth || 0)
              ? table.getCenterTotalSize()
              : tableContainerRef.current?.clientWidth,
        }}
      >
        <TableHeader className="sticky top-0 z-[1]">
          {table.getHeaderGroups().map((headerGroup) => (
            <TableRow
              key={headerGroup.id}
              className="flex w-full bg-background hover:bg-background"
            >
              {headerGroup.headers.map((header) => {
                return (
                  <TableHead
                    key={header.id}
                    colSpan={header.colSpan}
                    style={{
                      width: header.getSize(),
                    }}
                    className={cn(
                      'relative grow bg-secondary/50',
                      (header.column.columnDef?.meta as MetaType)?.headerClassName,
                    )}
                  >
                    {header.isPlaceholder ? null : (
                      <div
                        className={cn('flex items-center h-full', {
                          'cursor-pointer': header.column.getCanSort(),
                        })}
                        onClick={header.column.getToggleSortingHandler()}
                      >
                        <div className="truncate">
                          {flexRender(header.column.columnDef.header, header.getContext())}
                        </div>
                        {header.column.getCanSort() && (
                          <div className="flex-none">
                            {{
                              asc: <LuArrowUp />,
                              desc: <LuArrowDown />,
                            }[header.column.getIsSorted() as string] ?? null}
                          </div>
                        )}
                      </div>
                    )}
                    {header.column.getCanResize() && (
                      <div
                        className={cn(
                          'absolute top-[15%] w-0.5 h-[70%] right-0 cursor-col-resize select-none touch-none bg-input opacity-50',
                          {
                            '': header.column.getIsResizing(),
                          },
                        )}
                        {...{
                          onDoubleClick: () => header.column.resetSize(),
                          onMouseDown: header.getResizeHandler(),
                          onTouchStart: header.getResizeHandler(),
                        }}
                      />
                    )}
                  </TableHead>
                );
              })}
            </TableRow>
          ))}
        </TableHeader>
        <TableBody
          style={{
            height: `${rowVirtualizer.getTotalSize()}px`, //tells scrollbar how big the table is
            position: 'relative', //needed for absolute positioning of rows
          }}
        >
          {rowVirtualizer.getVirtualItems().length ? (
            rowVirtualizer.getVirtualItems().map((virtualRow, i) => {
              const row = rows[virtualRow.index];
              const rowClass =
                typeof rowClassName === 'function' ? rowClassName(row) : rowClassName;
              return (
                <TableRow
                  ref={(node) => rowVirtualizer.measureElement(node)}
                  key={row.id}
                  data-state={row.getIsSelected() && 'selected'}
                  data-index={virtualRow.index}
                  className={cn(
                    {
                      'cursor-pointer': onClickRow || onChangeRowSelection,
                    },
                    rowClass,
                  )}
                  style={{
                    display: 'flex',
                    position: 'absolute',
                    transform: `translateY(${virtualRow.start}px)`, //this should always be a `style` as it changes on scroll
                    width: '100%',
                  }}
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
                  {row.getVisibleCells().map((cell) => {
                    return (
                      <TableCell
                        key={cell.id}
                        style={{ width: cell.column.getSize() }}
                        className={cn(
                          'grow',
                          (cell.column.columnDef?.meta as MetaType)?.cellClassName,
                          {
                            truncate: enableColumnResizing,
                          },
                        )}
                        onClick={() => onClickCell?.(cell)}
                        onDoubleClick={() => onDoubleClickCell?.(cell)}
                      >
                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                      </TableCell>
                    );
                  })}
                </TableRow>
              );
            })
          ) : (
            <TableRow>
              <TableCell colSpan={columns.length} className="h-24 text-center">
                {loading ? 'Loading...' : 'No results.'}
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>
  );
}
