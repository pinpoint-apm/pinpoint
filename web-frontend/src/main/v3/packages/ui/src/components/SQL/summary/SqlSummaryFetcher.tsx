import React from 'react';
import { useSetAtom } from 'jotai';
import { sqlSelectedSummaryDatasAtom } from '@pinpoint-fe/atoms';
import { useGetSqlStatSummaryData, useSqlStatSearchParameters } from '@pinpoint-fe/hooks';
import { DataTable, DataTableCountOfRows } from '../../DataTable';
import { summaryColumns } from './sqlSummaryTableColumns';
import { cn } from '../../../lib';

export interface SqlSummaryFetcherProps {
  className?: string;
}

export const SqlSummaryFetcher = ({ className }: SqlSummaryFetcherProps) => {
  const setSqlSelectedSummaryDatas = useSetAtom(sqlSelectedSummaryDatasAtom);
  const [count, setCount] = React.useState(50);
  const [orderBy, setOrderBy] = React.useState('totalCount');
  const [isDesc, setIsDesc] = React.useState(true);
  const { groupBy } = useSqlStatSearchParameters();
  const { data } = useGetSqlStatSummaryData({
    count,
    isDesc,
    orderBy,
  });
  const enableMultiRowSelection = groupBy === undefined || groupBy === 'query' ? false : true;
  const columns = summaryColumns({
    orderBy,
    isDesc,
    groupBy,
    enableMultiRowSelection,
    onClickColumnHeader: (key) => {
      if (orderBy === key) {
        setIsDesc(!isDesc);
      } else {
        setIsDesc(true);
      }

      setOrderBy(key);
    },
  });

  return (
    <>
      <DataTableCountOfRows
        triggerClassName="mt-10 mb-2"
        selectedCount={count}
        onChange={(c) => setCount(c)}
      />
      <div className={cn('max-h-[calc(100%-26rem)] rounded-md border', className)}>
        <DataTable
          tableClassName="text-xs"
          columns={columns}
          data={data || []}
          enableMultiRowSelection={enableMultiRowSelection}
          onChangeRowSelection={(datas) => {
            setSqlSelectedSummaryDatas(datas);
          }}
        />
      </div>
    </>
  );
};
