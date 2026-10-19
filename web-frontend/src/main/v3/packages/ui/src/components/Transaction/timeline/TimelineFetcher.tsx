import { useGetTraceViewerData, useTransactionSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { TimelineCore, TimelineCoreProps } from './TimelineCore';

export interface TimelineFetcherProps extends Omit<TimelineCoreProps, 'traceViewerData'> {}

export const TimelineFetcher = (props: TimelineFetcherProps) => {
  const { transactionInfo } = props;
  const { transactionInfo: transactionSearchParams } = useTransactionSearchParameters();
  const { data } = useGetTraceViewerData({
    traceId: transactionInfo?.transactionId,
    spanId: `${transactionInfo?.spanId}`,
    agentId: transactionInfo?.agentId,
    focusTimestamp: transactionSearchParams?.focusTimestamp,
    linkTraceId: transactionSearchParams?.linkTraceId,
    linkSpanId: transactionSearchParams?.linkSpanId,
  });

  return <TimelineCore {...props} traceViewerData={data} />;
};
