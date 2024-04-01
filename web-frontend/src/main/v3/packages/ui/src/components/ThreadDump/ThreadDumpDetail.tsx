import React from 'react';
import { ErrorBoundary } from '../Error/ErrorBoundary';
import { ThreadDumpListSkeleton } from './ThreadDumpListSkeleton';
import { ThreadDumpDetailFetcher, ThreadDumpDetailFetcherProps } from './ThreadDumpDetailFetcher';

export interface ThreadDumpDetailProps extends ThreadDumpDetailFetcherProps {}

export const ThreadDumpDetail = (props: ThreadDumpDetailProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ThreadDumpListSkeleton />}>
        <ThreadDumpDetailFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
