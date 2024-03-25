import React from 'react';
import { ServerMapSkeleton } from './ServerMapSkeleton';
import { ErrorBoundary, ServerMapFetcher, ServerMapFetcherProps } from '..';

export const ServerMap = ({ ...props }: ServerMapFetcherProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ServerMapSkeleton className="w-full h-full" />}>
        <ServerMapFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
