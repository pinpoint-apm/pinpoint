import React from 'react';
import { ErrorBoundary } from '../Error/ErrorBoundary';
import { HostListFetcher, HostListFetcherProps } from './HostListFetcher';
import { ListItemSkeleton } from '../VirtualList';

export interface HostListProps extends HostListFetcherProps {}

export const HostList = ({ ...props }: HostListProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense
        fallback={
          <ListItemSkeleton
            className="border border-t-0 rounded-b h-min"
            skeletonOption={{ viewBoxHeight: 160 }}
          />
        }
      >
        <HostListFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
