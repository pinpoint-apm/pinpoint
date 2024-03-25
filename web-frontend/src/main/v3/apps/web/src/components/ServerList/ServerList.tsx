import React from 'react';
import { ErrorBoundary, ServerListSkeleton } from '@pinpoint-fe/ui';
import { ServerListFetcher, ServerListFetcherProps } from './ServerListFetcher';

export const ServerList = ({ ...props }: ServerListFetcherProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense
        fallback={
          <div className="flex h-full">
            <ServerListSkeleton className="h-full border-t border-r" />
          </div>
        }
      >
        <ServerListFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
