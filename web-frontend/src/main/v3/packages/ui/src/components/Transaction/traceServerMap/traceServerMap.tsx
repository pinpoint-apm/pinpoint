import React from 'react';
import { ErrorBoundary, ServerMapCore, ServerMapSkeleton } from '../../../components';
import { getBaseNodeId } from '@pinpoint-fe/ui/src/utils';
import {
  useGetTransactionTraceServerMap,
  useTransactionSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

export interface TraceServerMapFetcherProps {
  configuration?: Configuration;
}

export interface TraceServerMapProps extends TraceServerMapFetcherProps {}

export const TraceServerMap = (props: TraceServerMapProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ServerMapSkeleton className="w-full h-full" />}>
        <TraceServerMapFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};

export const TraceServerMapFetcher = ({ configuration }: TraceServerMapFetcherProps) => {
  const { application } = useTransactionSearchParameters();
  const { data } = useGetTransactionTraceServerMap();
  const enableServiceMap = configuration?.['experimental.enableServiceMap.value'] ?? false;

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
        enableServiceMap,
      })}
      disableMenu
    />
  );
};
