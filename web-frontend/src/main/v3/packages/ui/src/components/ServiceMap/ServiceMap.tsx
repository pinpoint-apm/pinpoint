import React from 'react';
import { ServerMapSkeleton } from '../ServerMap/ServerMapSkeleton';
import { ErrorBoundary } from '..';
import { ServiceMapFetcher, ServiceMapFetcherProps } from './ServiceMapFetcher';

export const ServiceMap = ({ ...props }: ServiceMapFetcherProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ServerMapSkeleton className="w-full h-full" />}>
        <ServiceMapFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
