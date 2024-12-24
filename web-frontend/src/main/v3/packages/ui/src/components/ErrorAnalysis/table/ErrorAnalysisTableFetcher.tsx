import React from 'react';
import { Row } from '@tanstack/react-table';
import { ErrorAnalysisErrorList } from '@pinpoint-fe/ui/constants';
import { useGetErrorAnalysisErrorListData } from '@pinpoint-fe/ui/hooks';
import { DataTable, DataTableCountOfRows } from '../../DataTable';
import { errorTableColumns } from './errorAnalysisTableColumns';
import { cn } from '../../../lib';
import { TooltipProvider } from '../../ui';

export interface ErrorAnalysisTableFetcherProps {
  className?: string;
  onClickRow?: (row: Row<ErrorAnalysisErrorList.ErrorData>) => void;
}

export const ErrorAnalysisTableFetcher = ({
  className,
  onClickRow,
}: ErrorAnalysisTableFetcherProps) => {
  const [count, setCount] = React.useState(50);
  const [orderBy, setOrderBy] = React.useState<string>();
  const [isDesc, setIsDesc] = React.useState<boolean>();
  const { data } = useGetErrorAnalysisErrorListData({
    orderBy,
    isDesc,
    count,
  });

  const columns = errorTableColumns({
    orderBy,
    isDesc,
    onClickColumnHeader: (key, order) => {
      if (order === undefined) {
        if (orderBy === key) {
          setIsDesc(!isDesc);
        } else {
          setIsDesc(true);
        }
      } else {
        setIsDesc(order);
      }

      setOrderBy(key);
    },
  });

  return (
    <TooltipProvider delayDuration={100}>
      <DataTableCountOfRows
        triggerClassName="mt-10 mb-2 bg-white"
        selectedCount={count}
        onChange={(c) => setCount(c)}
      />
      <div className={cn('rounded-md border bg-white', className)}>
        <DataTable
          tableClassName="[&>tbody]:text-xs"
          columns={columns}
          data={data || []}
          onClickRow={(row) => onClickRow?.(row)}
        />
      </div>
    </TooltipProvider>
  );
};
