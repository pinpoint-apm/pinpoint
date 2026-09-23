import React from 'react';
import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from '../components/ui/resizable';
import { TransactionInfo, TransactionInfoProps, TransactionList } from '../components/Transaction';
import { MainHeader, ApplicationCombinedList, Separator } from '../components';
import { useAtomValue } from 'jotai';
import { useIsForbiddenPath, useTransactionSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { Forbidden403 } from './Forbidden403';
import { transactionListDatasAtom } from '@pinpoint-fe/ui/src/atoms';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';
import { PiStackDuotone } from 'react-icons/pi';
import { TransactionListProgressBar } from '../components/Transaction/transaction-list/TransactionListProgressBar';
import { TransactionListByFilterMap } from '../components/Transaction/transaction-list/TransactionListByFilterMap';
import { TransactionListByTrace } from '../components/Transaction/transaction-list/TransactionListByTrace';

export interface TransactionListPageProps {
  transactionInfoProps?: TransactionInfoProps;
}

export const TransactionListPage = ({ transactionInfoProps }: TransactionListPageProps) => {
  const { application, withFilter, traceInfo } = useTransactionSearchParameters();
  const isForbidden = useIsForbiddenPath();
  const [nextX2, setNextX2] = React.useState<number>();
  const [nextDataIndex, setNextDataIndex] = React.useState<number>(99);
  const transactionListData = useAtomValue(transactionListDatasAtom);

  const handleClickResume = () => {
    if (withFilter) {
      setNextDataIndex(nextDataIndex + 100);
    } else {
      if (transactionListData?.resultFrom) {
        setNextX2(transactionListData.resultFrom - 1);
      }
    }
  };

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        className="shadow-sm"
        title={
          <div className="flex items-center gap-2">
            <PiStackDuotone /> Transaction list
          </div>
        }
      >
        <ApplicationCombinedList selectedApplication={application} disabled />
        {!traceInfo && (
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
        )}
      </MainHeader>
      {/* 이 화면의 API 중 하나가 403을 받았다. **헤더는 남기고 본문만 바꾼다** — 본문을 언마운트해
          남은 조회들도 함께 멈춘다. 지금 이 화면이 부르는 API에는 권한 검사가 없지만, 나중에
          붙었을 때 조용히 빠지지 않도록 다른 화면과 같은 처리를 둔다.
          (어느 화면이 이 판정에서 빠지는지는 `coversPageOnForbidden`. 이슈 #10744) */}
      {isForbidden && <Forbidden403 />}
      {!isForbidden && (
        <>
          {!traceInfo && (
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
          )}
          <Separator />
          <ResizablePanelGroup
            direction="vertical"
            autoSaveId={APP_SETTING_KEYS.TRANSACTION_LIST_RESIZABLE}
          >
            <ResizablePanel id="list" minSize={10} maxSize={90}>
              {traceInfo ? (
                <TransactionListByTrace />
              ) : withFilter ? (
                <TransactionListByFilterMap nextDataIndex={nextDataIndex} />
              ) : (
                <TransactionList params={{ x2: nextX2 }} />
              )}
            </ResizablePanel>
            <ResizableHandle withHandle />
            <ResizablePanel id="info" minSize={10} maxSize={90}>
              <TransactionInfo {...transactionInfoProps} />
            </ResizablePanel>
          </ResizablePanelGroup>
        </>
      )}
    </div>
  );
};
