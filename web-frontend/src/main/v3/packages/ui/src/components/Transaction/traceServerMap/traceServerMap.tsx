import React from 'react';
import { ErrorBoundary, ServerMapCore, ServerMapSkeleton } from '../../../components';
import { getBaseNodeId } from '@pinpoint-fe/ui/src/utils';
import {
  useGetTransactionTraceServerMap,
  useTransactionSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';

export interface TraceServerMapProps extends TraceServerMapFetcherProps {}

export const TraceServerMap = (props: TraceServerMapProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ServerMapSkeleton />}>
        <TraceServerMapFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};

export interface TraceServerMapFetcherProps {}

export const TraceServerMapFetcher = ({}: TraceServerMapFetcherProps) => {
  const { application } = useTransactionSearchParameters();
  const { data } = useGetTransactionTraceServerMap();

  return (
    <ServerMapCore
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-ignore
      data={data}
      baseNodeId={getBaseNodeId({
        application: application,
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-ignore
        applicationMapData: data.applicationMapData,
      })}
      disableMenu
    />
  );
};
