import React from 'react';
import { ErrorBoundary } from '../../Error';
import { ChartSkeleton } from '../../Chart';
import { AgentChartFetcher, AgentDataSourceChart, AGENT_CHART_ID_LIST } from '.';
import { InspectorChart } from './InspectorChart';

export interface InspectorAgentChartListProps {
  emptyMessage?: string;
  agentChartList?: typeof AGENT_CHART_ID_LIST;
}

export const InspectorAgentChartList = ({
  emptyMessage,
  agentChartList = AGENT_CHART_ID_LIST,
}: InspectorAgentChartListProps) => {
  return (
    <>
      <div className="grid gap-4 md:grid-cols-1 lg:grid-cols-2 xl:grid-cols-3">
        {agentChartList.map((chartId) => (
          <ErrorBoundary key={chartId}>
            <React.Suspense
              fallback={
                <ChartSkeleton skeletonOption={{ viewBoxWidth: 800, viewBoxHeight: 450 }} />
              }
            >
              <AgentChartFetcher chartId={chartId} emptyMessage={emptyMessage}>
                {(props) => <InspectorChart {...props} />}
              </AgentChartFetcher>
            </React.Suspense>
          </ErrorBoundary>
        ))}
      </div>
      <div className="mt-2">
        <ErrorBoundary>
          <React.Suspense
            fallback={<ChartSkeleton skeletonOption={{ viewBoxWidth: 1300, viewBoxHeight: 400 }} />}
          >
            <AgentDataSourceChart className="w-full h-80" emptyMessage={emptyMessage} />
          </React.Suspense>
        </ErrorBoundary>
      </div>
    </>
  );
};
