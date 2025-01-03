import React from 'react';
import { SWRConfiguration } from 'swr';
import { useAtom } from 'jotai';
import { useGetHeatmapDrag } from '@pinpoint-fe/ui/hooks';
import { TransactionListTable, TransactionListTableProps } from '../..';
import { transactionListDatasAtom } from '@pinpoint-fe/ui/atoms';
import { HeatmapDrag } from '@pinpoint-fe/ui/constants';

export interface TransactionListFetcherProps extends TransactionListTableProps {
  params?: Partial<HeatmapDrag.Parameters>;
}

export const TransactionListFetcher = ({ params, ...props }: TransactionListFetcherProps) => {
  const [swrOption, setSwrOption] = React.useState<SWRConfiguration>();
  const { data } = useGetHeatmapDrag(params, swrOption);
  const [transactionListData, setTransactionListDataAtom] = useAtom(transactionListDatasAtom);

  React.useEffect(() => {
    if (data) {
      setSwrOption({ suspense: false });
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
