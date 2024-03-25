import React from 'react';
import { FilteredMapFetcher, FilteredMapFetcherProps } from './FilteredMapFetcher';
import { ErrorBoundary, ServerMapSkeleton } from '..';

export const FilteredMap = ({ ...props }: FilteredMapFetcherProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ServerMapSkeleton className="w-full h-full" />}>
        <FilteredMapFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
