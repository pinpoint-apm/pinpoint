import React from 'react';
import { ErrorBoundary } from '../../../Error/ErrorBoundary';
import {
  InspectorAgentStatusTimelineFetcher,
  InspectorAgentStatusTimelineFetcherProps,
} from './InspectorAgentStatusTimelineFetcher';
import { TimelineSkeleton } from '../TimelineSkeleton';

export interface InspectorAgentStatusTimelineProps
  extends InspectorAgentStatusTimelineFetcherProps {}

export const InspectorAgentStatusTimeline = ({ ...props }: InspectorAgentStatusTimelineProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<TimelineSkeleton />}>
        <InspectorAgentStatusTimelineFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
