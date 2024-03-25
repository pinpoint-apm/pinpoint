import React from 'react';
import { ErrorBoundary } from '../Error/ErrorBoundary';
import { AgentListFetcher, AgentListFetcherProps } from './AgentListFetcher';
import { AgentListSkeleton } from './AgentListSkeleton';

export interface AgentListProps extends AgentListFetcherProps {}

export const AgentList = ({ ...props }: AgentListProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<AgentListSkeleton className="border border-t-0 rounded-b" />}>
        <AgentListFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
