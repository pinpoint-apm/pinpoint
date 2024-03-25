import React from 'react';
import { ApdexScoreFetcher, ApdexScoreFetcherProps } from './ApdexScoreFetcher';
import { ApdexSkeleton, ErrorBoundary } from '..';

export interface ApdexScoreProps extends ApdexScoreFetcherProps {}

export const ApdexScore = (props: ApdexScoreProps) => {
  return (
    <ErrorBoundary>
      <React.Suspense fallback={<ApdexSkeleton />}>
        <ApdexScoreFetcher {...props} />
      </React.Suspense>
    </ErrorBoundary>
  );
};
