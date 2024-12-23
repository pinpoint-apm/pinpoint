import { ResizableHandle, ResizablePanel, ResizablePanelGroup } from '../components/ui/resizable';
import { TransactionInfo, TransactionInfoProps } from '../components/Transaction';
import { MainHeader, ApplicationCombinedList } from '../components';
import { PiStackDuotone } from 'react-icons/pi';
import { useAtomValue } from 'jotai';
import { FaChevronRight } from 'react-icons/fa6';
import { useTransactionSearchParameters } from '@pinpoint-fe/ui/hooks';
import { transactionInfoDatasAtom } from '@pinpoint-fe/ui/atoms';
import { APP_SETTING_KEYS } from '@pinpoint-fe/constants';
import { TransactionCharts } from '../components/Transaction/charts/TransactionCharts';

export interface TransactionDetailPageProps {
  transactionInfoProps?: TransactionInfoProps;
}

export const TransactionDetailPage = ({ transactionInfoProps }: TransactionDetailPageProps) => {
  const { application } = useTransactionSearchParameters();
  const transactionInfoData = useAtomValue(transactionInfoDatasAtom);

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        className="shadow"
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
            <div className="truncate">{transactionInfoData.applicationName}</div>
            <FaChevronRight className="fill-slate-400 mx-1.5 flex-none" />
            <div className="truncate text-muted-foreground">
              {transactionInfoData.transactionId}
            </div>
          </div>
        )}
      </MainHeader>
      <ResizablePanelGroup
        direction="vertical"
        autoSaveId={APP_SETTING_KEYS.TRANSACTION_DETAIL_RESIZABLE}
      >
        <ResizablePanel minSize={20} maxSize={40}>
          {application && <TransactionCharts />}
        </ResizablePanel>
        <ResizableHandle className="!h-2" withHandle />
        <ResizablePanel>
          <TransactionInfo disableHeader {...transactionInfoProps} />
        </ResizablePanel>
      </ResizablePanelGroup>
    </div>
  );
};
