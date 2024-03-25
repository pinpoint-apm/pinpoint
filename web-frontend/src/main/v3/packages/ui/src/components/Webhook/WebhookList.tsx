import React from 'react';
import { ErrorBoundary } from '../Error/ErrorBoundary';
import { WebhookListFetcher, WebhookListFetcherProps } from './WebhookListFetcher';
import { DataTableSkeleton } from '../../components/DataTable';

export interface WebhookListProps extends WebhookListFetcherProps {}

export const WebhookList = (props: WebhookListProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<DataTableSkeleton hideRowBox />}>
        <WebhookListFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
