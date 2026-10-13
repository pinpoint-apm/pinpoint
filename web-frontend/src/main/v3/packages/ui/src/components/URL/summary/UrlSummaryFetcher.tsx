import React from 'react';
import { useAtom } from 'jotai';
import { urlSelectedSummaryDataAtom } from '@pinpoint-fe/ui/src/atoms';
import { useGetUrlStatSummaryData } from '@pinpoint-fe/ui/src/hooks';
import { summaryColumns } from './UrlSummaryColumns';
import { DataTableCountOfRows, VirtualizedDataTable } from '../../DataTable';
import { cn } from '../../../lib';
import { UrlStatSummary } from '@pinpoint-fe/ui/src/constants';

export interface UrlSummaryFetcherProps {
  type?: UrlStatSummary.Parameters['type'];
  className?: string;
}

export const UrlSummaryFetcher = ({ className, type }: UrlSummaryFetcherProps) => {
  const [urlSelectedSummaryData, setUrlSelectedSummaryData] = useAtom(urlSelectedSummaryDataAtom);
  const [count, setCount] = React.useState(50);
  const [orderBy, setOrderBy] = React.useState('totalCount');
  const [isDesc, setIsDesc] = React.useState(true);
  const { data } = useGetUrlStatSummaryData({
    count,
    isDesc,
    orderBy,
    type,
  });
  const columns = React.useMemo(
    () =>
      summaryColumns({
        orderBy,
        isDesc,
        onClickColumnHeader: (key) => {
          if (orderBy === key) {
            setIsDesc((prev) => !prev);
          } else {
            setIsDesc(true);
          }
          setOrderBy(key);
        },
      }),
    [orderBy, isDesc],
  );

  const getRowSelectionInfo = () => {
    const selectedRowIndex =
      data?.findIndex(({ uri }) => uri === urlSelectedSummaryData?.uri) ?? -1;

    return selectedRowIndex === -1 ? {} : { [selectedRowIndex]: true };
  };

  return (
    <>
      <DataTableCountOfRows
        triggerClassName="mt-10 mb-2 shrink-0"
        selectedCount={count}
        onChange={(c) => setCount(c)}
      />
      {/* 테이블이 자기 안에서 스크롤돼야 가상화와 sticky 헤더가 동작한다(스크롤 요소가 테이블 컨테이너다).
          남은 높이가 너무 작은 창에서는 20rem 아래로 줄이지 않고 페이지 스크롤로 넘긴다. */}
      <div
        className={cn('max-h-[max(calc(100%-26rem),20rem)] rounded-md border bg-white', className)}
      >
        <VirtualizedDataTable
          tableClassName="text-xs"
          columns={columns}
          data={data || []}
          rowSelectionInfo={getRowSelectionInfo()}
          onChangeRowSelection={(data) => {
            setUrlSelectedSummaryData(data[0]);
          }}
        />
      </div>
    </>
  );
};
