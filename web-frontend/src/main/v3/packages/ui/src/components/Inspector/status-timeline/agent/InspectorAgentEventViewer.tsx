import React from 'react';
import { ErrorBoundary } from '../../../Error/ErrorBoundary';
import {
  InspectorAgentEventViewerFetcherProps,
  InspectorAgentEventViewerFetcher,
} from './InspectorAgentEventViewerFetcher';

export interface InspectorAgentEventViewerProps extends InspectorAgentEventViewerFetcherProps {}

export const InspectorAgentEventViewer = ({ ...props }: InspectorAgentEventViewerProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={'Loading...'}>
        <InspectorAgentEventViewerFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
