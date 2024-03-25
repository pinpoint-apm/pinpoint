import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import {
  InspectorAgentInfoFetcher,
  InspectorAgentInfoFetcherProps,
} from './InspectorAgentInfoFetcher';
import { InspectorAgentInfoSkeleton } from './InspectorAgentInfoSkeleton';

export interface InspectorAgentInfoProps extends InspectorAgentInfoFetcherProps {}

export const InspectorAgentInfo = ({ ...props }: InspectorAgentInfoProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<InspectorAgentInfoSkeleton />}>
        <InspectorAgentInfoFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
