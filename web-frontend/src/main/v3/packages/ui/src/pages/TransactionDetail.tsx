import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from '../components/ui/resizable';
import { TransactionInfo, TransactionInfoProps } from '../components/Transaction';
import { MainHeader, ApplicationCombinedList } from '../components';
import { PiStackDuotone } from 'react-icons/pi';
import { useAtomValue } from 'jotai';
import { FaChevronRight } from 'react-icons/fa6';
import { useIsForbiddenPath, useTransactionSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { Forbidden403 } from './Forbidden403';
import { transactionInfoDatasAtom } from '@pinpoint-fe/ui/src/atoms';
import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';
import { TransactionCharts } from '../components/Transaction/charts/TransactionCharts';

export interface TransactionDetailPageProps {
  transactionInfoProps?: TransactionInfoProps;
}

export const TransactionDetailPage = ({ transactionInfoProps }: TransactionDetailPageProps) => {
  const { application } = useTransactionSearchParameters();
  const transactionInfoData = useAtomValue(transactionInfoDatasAtom);
  const isForbidden = useIsForbiddenPath();

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        className="shadow-sm"
        title={
          <div className="flex items-center gap-2">
            <PiStackDuotone /> Transaction Detail
          </div>
        }
      >
        <ApplicationCombinedList selectedApplication={application} disabled />
        {transactionInfoData && (
          <div className="flex items-center gap-1 ml-4 text-sm font-semibold truncate">
            <div className="truncate">({transactionInfoData.agentId})</div>
            <FaChevronRight className="fill-slate-400 mx-1.5 flex-none" />
            <div className="truncate">{transactionInfoData.uri}</div>
            <FaChevronRight className="fill-slate-400 mx-1.5 flex-none" />
            <div className="truncate text-muted-foreground">
              {transactionInfoData.transactionId}
            </div>
          </div>
        )}
      </MainHeader>
      {/* 이 화면의 API 중 하나가 403을 받았다. **헤더는 남기고 본문만 바꾼다** — 본문을 언마운트해
          남은 조회들도 함께 멈춘다. 이 화면의 선택 박스는 표시 전용(disabled)이라 대상은 경로로만
          바뀌고, 경로가 바뀌면 판정도 함께 버려진다(`useClearForbiddenOnPathChange`).
          (어느 화면이 이 판정에서 빠지는지는 `coversPageOnForbidden`. 이슈 #10744) */}
      {isForbidden && <Forbidden403 />}
      {!isForbidden && (
        <ResizablePanelGroup
          direction="vertical"
          autoSaveId={APP_SETTING_KEYS.TRANSACTION_DETAIL_RESIZABLE}
        >
          <ResizablePanel id="charts" minSize={20} maxSize={40}>
            {application && <TransactionCharts />}
          </ResizablePanel>
          <ResizableHandle withHandle />
          <ResizablePanel id="detail" minSize={60} maxSize={80}>
            <TransactionInfo disableHeader {...transactionInfoProps} />
          </ResizablePanel>
        </ResizablePanelGroup>
      )}
    </div>
  );
};
