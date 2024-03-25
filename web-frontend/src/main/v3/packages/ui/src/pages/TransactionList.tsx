import React from 'react';
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from '../components/ui/resizable';
import { TransactionInfo, TransactionInfoProps, TransactionList } from '../components/Transaction';
import {
  MainHeader,
  ApplicationCombinedList,
  ProgressBarWithControls,
  Separator,
} from '../components';
import { PiStackFill } from 'react-icons/pi';
import { useAtomValue } from 'jotai';
import { differenceInMinutes } from 'date-fns';
import { useTransactionSearchParameters } from '@pinpoint-fe/hooks';
import { transactionListDatasAtom } from '@pinpoint-fe/atoms';
import { APP_SETTING_KEYS } from '@pinpoint-fe/constants';

export interface TransactionListPageProps {
  transactionInfoProps?: TransactionInfoProps;
}

export const TransactionListPage = ({ transactionInfoProps }: TransactionListPageProps) => {
  const { application, dateRange } = useTransactionSearchParameters();
  const [nextX2, setNextX2] = React.useState<number>();
  const transactionListData = useAtomValue(transactionListDatasAtom);

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        className="shadow"
        title={
          <div className="flex items-center gap-2">
            <PiStackFill /> Transaction list
          </div>
        }
      >
        <ApplicationCombinedList selectedApplication={application} disabled />
        <ProgressBarWithControls
          className="hidden w-full pt-4 ml-10 xl:flex"
          progress={
            transactionListData?.complete
              ? dateRange.from.getTime()
              : transactionListData?.resultFrom
          }
          range={[dateRange.to.getTime(), dateRange.from.getTime()]}
          tickCount={
            differenceInMinutes(dateRange.to, dateRange.from) < 4
              ? differenceInMinutes(dateRange.to, dateRange.from)
              : 4
          }
          onClickResume={() =>
            transactionListData?.resultFrom && setNextX2(transactionListData.resultFrom - 1)
          }
        >
          {({ isComplete, completeRenderer, resumeRenderer }) =>
            isComplete ? completeRenderer : resumeRenderer
          }
        </ProgressBarWithControls>
      </MainHeader>
      <ProgressBarWithControls
        className="flex w-full pt-4 mx-2 xl:hidden"
        progress={
          transactionListData?.complete ? dateRange.from.getTime() : transactionListData?.resultFrom
        }
        range={[dateRange.to.getTime(), dateRange.from.getTime()]}
        tickCount={
          differenceInMinutes(dateRange.to, dateRange.from) < 4
            ? differenceInMinutes(dateRange.to, dateRange.from)
            : 4
        }
        onClickResume={() =>
          transactionListData?.resultFrom && setNextX2(transactionListData.resultFrom - 1)
        }
      >
        {({ isComplete, completeRenderer, resumeRenderer }) =>
          isComplete ? completeRenderer : resumeRenderer
        }
      </ProgressBarWithControls>
      <Separator />
      <ResizablePanelGroup
        direction="vertical"
        autoSaveId={APP_SETTING_KEYS.TRANSACTION_LIST_RESIZABLE}
      >
        <ResizablePanel>
          <TransactionList params={{ x2: nextX2 }} />
        </ResizablePanel>
        <ResizableHandle className="!h-2" withHandle />
        <ResizablePanel>
          <TransactionInfo {...transactionInfoProps} />
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  );
};
