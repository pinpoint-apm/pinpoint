import React from 'react';
import { ErrorBoundary } from '../../Error/ErrorBoundary';
import { UrlStatChartFetcher, UrlStatChartFetcherProps } from './UrlStatChartFetcher';
import { ChartSkeleton } from '../../Chart';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../../ui';

export interface UrlStatChartProps extends UrlStatChartFetcherProps {}

export const UrlStatChart = ({ ...props }: UrlStatChartProps) => {
  const tabList = [
    { id: 'total', display: 'Total Count' },
    { id: 'failure', display: 'Failure Count' },
    { id: 'apdex', display: 'Apdex' },
    { id: 'latency', display: 'Latency' },
  ];
  return (
    <Tabs defaultValue="total">
      <TabsList>
        {tabList.map((tab) => (
          <TabsTrigger key={tab.id} value={tab.id}>
            {tab.display}
          </TabsTrigger>
        ))}
      </TabsList>
      {tabList.map((tab) => (
        <TabsContent key={tab.id} value={tab.id} className="h-72">
          <ErrorBoundary>
            <React.Suspense fallback={<ChartSkeleton />}>
              <UrlStatChartFetcher {...props} type={tab.id} />
            </React.Suspense>
          </ErrorBoundary>
        </TabsContent>
      ))}
    </Tabs>
  );
};
