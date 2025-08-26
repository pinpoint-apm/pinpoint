import React from 'react';
import { ErrorBoundary } from '../../../Error/ErrorBoundary';
import {
  InspectorAgentEventViewerFetcherProps,
  InspectorAgentEventViewerFetcher,
} from './InspectorAgentEventViewerFetcher';
import { TimelineSkeleton } from '../TimelineSkeleton';

export interface InspectorAgentEventViewerProps extends InspectorAgentEventViewerFetcherProps {}

export const InspectorAgentEventViewer = ({ ...props }: InspectorAgentEventViewerProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<TimelineSkeleton />}>
        <InspectorAgentEventViewerFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
