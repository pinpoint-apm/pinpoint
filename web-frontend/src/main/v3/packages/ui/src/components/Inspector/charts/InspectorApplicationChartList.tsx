import React from 'react';
import { ErrorBoundary } from '../../Error';
import { ChartSkeleton } from '../../Chart';
import { ApplicationChartFetcher, APPLICATION_CHART_ID_LIST, ApplicationDataSourceChart } from '.';
import { InspectorChart } from './InspectorChart';

export interface InspectorApplicationChartListProps {
  emptyMessage?: string;
  chartIdList?: typeof APPLICATION_CHART_ID_LIST;
}

export const InspectorApplicationChartList = ({
  emptyMessage,
  chartIdList = APPLICATION_CHART_ID_LIST,
}: InspectorApplicationChartListProps) => {
  return (
    <>
      <div className="grid gap-4 md:grid-cols-1 lg:grid-cols-2 xl:grid-cols-3">
        {chartIdList.map((chartId) => (
          <ErrorBoundary key={chartId}>
            <React.Suspense
              fallback={
                <ChartSkeleton skeletonOption={{ viewBoxWidth: 800, viewBoxHeight: 450 }} />
              }
            >
              <ApplicationChartFetcher chartId={chartId} emptyMessage={emptyMessage}>
                {(props) => <InspectorChart {...props} />}
              </ApplicationChartFetcher>
            </React.Suspense>
          </ErrorBoundary>
        ))}
      </div>
      <div className="mt-2">
        <ErrorBoundary>
          <React.Suspense
            fallback={<ChartSkeleton skeletonOption={{ viewBoxWidth: 1300, viewBoxHeight: 400 }} />}
          >
            <ApplicationDataSourceChart className="w-full h-80" emptyMessage={emptyMessage} />
          </React.Suspense>
        </ErrorBoundary>
      </div>
    </>
  );
};
