import React from 'react';
import { useAtom } from 'jotai';
import { TransactionListTable, TransactionListTableProps } from './TransactionListTable';
import { transactionListDatasAtom } from '@pinpoint-fe/atoms';
import { usePostTransactionMetaData, useTransactionSearchParameters } from '@pinpoint-fe/ui/hooks';
import { APP_PATH, SCATTER_DATA_TOTAL_KEY, ScatterDataByAgent } from '@pinpoint-fe/constants';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '../../ui';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';

declare global {
  interface Window {
    __pp_scatter_data__: ScatterDataByAgent['acc'] | undefined;
  }
}

export interface TransactionListByFilterMapFetcherProps extends TransactionListTableProps {
  nextDataIndex: number;
}

export const TransactionListByFilterMapFetcher = ({
  nextDataIndex,
  ...props
}: TransactionListByFilterMapFetcherProps) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { dragInfo, application } = useTransactionSearchParameters();
  const [transactionListData, setTransactionListDataAtom] = useAtom(transactionListDatasAtom);
  const prevDataIndex = React.useRef<number>(0);
  const { mutate } = usePostTransactionMetaData({
    onSuccess: (res) => {
      setTransactionListDataAtom((prev) => {
        return {
          metadata: [...(prev?.metadata || []), ...res.metadata],
          complete: res.metadata.length < 100,
        };
      });
    },
  });
  const openerScatterData = window.opener?.__pp_scatter_data__ as ScatterDataByAgent['acc'];

  React.useEffect(() => {
    if (openerScatterData) {
      console.log(openerScatterData);

      const datas = openerScatterData[dragInfo?.agentId || SCATTER_DATA_TOTAL_KEY]
        ?.filter((data) => {
          return (
            data.x >= dragInfo.x1 &&
            data.x <= dragInfo.x2 &&
            data.y >= dragInfo.y1 &&
            data.y <= dragInfo.y2
          );
        })
        ?.slice(prevDataIndex.current, nextDataIndex + 1);

      if (datas && datas?.length > 0) {
        const formData = new FormData();
        formData.append('ApplicationName', application?.applicationName || '');
        console.log(openerScatterData);

        datas
          .sort((a, b) => a.x - b.x)
          .forEach((data, i) => {
            formData.append(
              `I${i}`,
              `${data.agentId}^${data.collectorAcceptTime}^${data.transactionId}`,
            );
            formData.append(`T${i}`, `${data.x}`);
            formData.append(`R${i}`, `${data.y}`);
          });

        mutate(formData);
      }
      prevDataIndex.current = nextDataIndex + 1;
    }
  }, [nextDataIndex]);

  return (
    <>
      <TransactionListTable data={transactionListData?.metadata} {...props} />
      <Dialog
        defaultOpen={!openerScatterData}
        onOpenChange={(open) => {
          if (!open) {
            navigate(APP_PATH.SERVER_MAP);
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t('TRANSACTION_LIST.TRANSACTION_RETRIEVE_ERROR')}</DialogTitle>
            <DialogDescription>
              {t('TRANSACTION_LIST.TRANSACTION_RETRIEVE_ERROR_DESC')}
            </DialogDescription>
          </DialogHeader>
        </DialogContent>
      </Dialog>
    </>
  );
};
