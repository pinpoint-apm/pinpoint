import React from 'react';
import { useAtom } from 'jotai';
import { useGetHeatmapDrag } from '@pinpoint-fe/ui/src/hooks';
import { TransactionListTable, TransactionListTableProps } from '../..';
import { transactionListDatasAtom } from '@pinpoint-fe/ui/src/atoms';
import { HeatmapDrag } from '@pinpoint-fe/ui/src/constants';

export interface TransactionListFetcherProps extends TransactionListTableProps {
  params?: Partial<HeatmapDrag.Parameters>;
}

export const TransactionListFetcher = ({ params, ...props }: TransactionListFetcherProps) => {
  const { data } = useGetHeatmapDrag(params);
  const [transactionListData, setTransactionListDataAtom] = useAtom(transactionListDatasAtom);

  React.useEffect(() => {
    if (data) {
      setTransactionListDataAtom((prev) => {
        return {
          ...data,
          metadata: [...(prev?.metadata || []), ...data.metadata],
        };
      });
    }
  }, [data]);

  return <TransactionListTable data={transactionListData?.metadata} {...props} />;
};
