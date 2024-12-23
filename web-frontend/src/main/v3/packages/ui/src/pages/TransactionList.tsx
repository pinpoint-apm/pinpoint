import React from 'react';
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from '../components/ui/resizable';
import { TransactionInfo, TransactionInfoProps, TransactionList } from '../components/Transaction';
import { MainHeader, ApplicationCombinedList, Separator } from '../components';
import { useAtomValue } from 'jotai';
import { useTransactionSearchParameters } from '@pinpoint-fe/ui/hooks';
import { transactionListDatasAtom } from '@pinpoint-fe/ui/atoms';
import { APP_SETTING_KEYS } from '@pinpoint-fe/constants';
import { PiStackDuotone } from 'react-icons/pi';
import { TransactionListProgressBar } from '../components/Transaction/transaction-list/TransactionListProgressBar';
import { TransactionListByFilterMap } from '../components/Transaction/transaction-list/TransactionListByFilterMap';

export interface TransactionListPageProps {
  transactionInfoProps?: TransactionInfoProps;
}

export const TransactionListPage = ({ transactionInfoProps }: TransactionListPageProps) => {
  const { application, withFilter } = useTransactionSearchParameters();
  const [nextX2, setNextX2] = React.useState<number>();
  const [nextDataIndex, setNextDataIndex] = React.useState<number>(99);
  const transactionListData = useAtomValue(transactionListDatasAtom);

  const handleClickResume = () => {
    if (withFilter) {
      setNextDataIndex(nextDataIndex + 100);
    } else {
      transactionListData?.resultFrom && setNextX2(transactionListData.resultFrom - 1);
    }
  };

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        className="shadow"
        title={
          <div className="flex items-center gap-2">
            <PiStackDuotone /> Transaction list
          </div>
        }
      >
        <ApplicationCombinedList selectedApplication={application} disabled />
        <TransactionListProgressBar
          className="hidden ml-10 xl:flex"
          onClickResume={() => {
            handleClickResume();
          }}
        >
          {({ isComplete, completeRenderer, resumeRenderer }) =>
            isComplete ? completeRenderer : resumeRenderer
          }
        </TransactionListProgressBar>
      </MainHeader>
      <TransactionListProgressBar
        className="flex mx-2 xl:hidden"
        onClickResume={() => {
          handleClickResume();
        }}
      >
        {({ isComplete, completeRenderer, resumeRenderer }) =>
          isComplete ? completeRenderer : resumeRenderer
        }
      </TransactionListProgressBar>
      <Separator />
      <ResizablePanelGroup
        direction="vertical"
        autoSaveId={APP_SETTING_KEYS.TRANSACTION_LIST_RESIZABLE}
      >
        <ResizablePanel>
          {withFilter ? (
            <TransactionListByFilterMap nextDataIndex={nextDataIndex} />
          ) : (
            <TransactionList params={{ x2: nextX2 }} />
          )}
        </ResizablePanel>
        <ResizableHandle className="!h-2" withHandle />
        <ResizablePanel>
          <TransactionInfo {...transactionInfoProps} />
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  );
};
