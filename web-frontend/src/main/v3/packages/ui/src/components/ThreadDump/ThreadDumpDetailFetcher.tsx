import { ActiveThreadDump } from '@pinpoint-fe/constants';
import { useGetActiveThreadDump } from '@pinpoint-fe/hooks';
import { ClipboardCopyButton } from '../../components/Button/ClipboardCopyButton';
import { HighLightCode } from '../../components/HighLightCode/HighLightCode';
import { Separator } from '../../components';

export interface ThreadDumpDetailFetcherProps {
  thread?: ActiveThreadDump.ThreadDumpData;
}

export const ThreadDumpDetailFetcher = ({ thread }: ThreadDumpDetailFetcherProps) => {
  const { data } = useGetActiveThreadDump(thread);
  const detail =
    data?.code === -1
      ? `${data.message}`
      : (data?.message.threadDumpData.length || 0) > 0
        ? data?.message.threadDumpData[0].detailMessage
        : 'There is no message(may be completed)';

  return (
    <>
      {data ? (
        <div className="flex flex-col h-full">
          <div className="flex items-center gap-1 px-4 py-2">
            <span className="font-semibold text">{thread?.threadName || 'temp'}</span>{' '}
            <ClipboardCopyButton
              copyValue={detail || ''}
              containerClassName="flex items-center ml-auto"
              btnClassName="border-none shadow-none w-8 h-8 text-muted-foreground"
            />
          </div>
          <Separator />
          <div className="relative flex-1 h-full p-4 overflow-hidden">
            <HighLightCode
              code={detail}
              language={'java'}
              className="h-full p-2 overflow-auto text-xs"
            />
          </div>
        </div>
      ) : (
        <div className="flex items-center justify-center w-full h-full">Select thread.</div>
      )}
    </>
  );
};
