import React from 'react';
import { ErrorBoundary } from '../../../Error/ErrorBoundary';
import {
  InspectorApplicationStatusTimelineFetcherProps,
  InspectorApplicationStatusTimelineFetcher,
} from './InspectorApplicationStatusTimelineFetcher';
import { TimelineSkeleton } from '../TimelineSkeleton';

export interface InspectorApplicationStatusTimelineProps
  extends InspectorApplicationStatusTimelineFetcherProps {}

export const InspectorApplicationStatusTimeline = ({
  ...props
}: InspectorApplicationStatusTimelineProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<TimelineSkeleton />}>
        <InspectorApplicationStatusTimelineFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
