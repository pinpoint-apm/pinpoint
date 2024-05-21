import React from 'react';
import { TimelineFetcher, TimelineFetcherProps } from './TimelineFetcher';
import { ErrorBoundary } from '../../../components';
import { TimelineSkeleton } from './TimelineSkeleton';

export interface TimelineProps extends TimelineFetcherProps {}

export const Timeline = (props: TimelineProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<TimelineSkeleton />}>
        <TimelineFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
