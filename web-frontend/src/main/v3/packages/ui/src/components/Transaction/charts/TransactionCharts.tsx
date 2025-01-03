import React from 'react';
import { ErrorBoundary } from '../../Error';
import {
  AGENT_CHART_ID,
  AgentChartFetcher,
  ChartSkeleton,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '../../../components';
import { useTransactionSearchParameters } from '@pinpoint-fe/ui/hooks';
import { addMinutes, subMinutes } from 'date-fns';
import { ChartCore } from '../../../components/Inspector/charts/ChartCore';
import { useBreakpoint } from '../../../lib/useBreakpoint';
import { useAtomValue } from 'jotai';
import { transactionInfoDatasAtom } from '@pinpoint-fe/ui/atoms';

const chartIdList: AGENT_CHART_ID[] = ['heap', 'nonHeap', 'cpu'];

export interface TransactionChartsProps {}

export const TransactionCharts = () => {
  const { isAboveXl, isBelowXl } = useBreakpoint('xl');
  const containerRef = React.useRef(null);
  const { transactionInfo } = useTransactionSearchParameters();
  const transactionInfoData = useAtomValue(transactionInfoDatasAtom);
  const focusTimestamp = transactionInfo.focusTimestamp || transactionInfoData?.callStackEnd;
  const fromDate = subMinutes(focusTimestamp, 10);
  const toDate = addMinutes(focusTimestamp, 10);

  React.useEffect(() => {
    const resizeObserver = new ResizeObserver(() => {
      window.dispatchEvent(new Event('resize'));
    });

    if (containerRef.current) {
      resizeObserver.observe(containerRef.current);
    }

    return () => {
      resizeObserver.disconnect();
    };
  }, []);

  if (!focusTimestamp) {
    return null;
  }

  return (
    <div ref={containerRef} className="h-full p-3">
      {isAboveXl && (
        <div className="grid h-full grid-cols-3 gap-4 text-xs">
          {chartIdList.map((chartId) => (
            <ErrorBoundary key={chartId}>
              <React.Suspense
                fallback={
                  <ChartSkeleton skeletonOption={{ viewBoxWidth: 800, viewBoxHeight: 450 }} />
                }
              >
                <AgentChartFetcher
                  fromDate={fromDate}
                  toDate={toDate}
                  chartId={chartId}
                  agentId={transactionInfo.agentId}
                >
                  {({ data, ...props }) => (
                    <div className="relative min-h-0">
                      <div className="absolute w-full text-center">{data.title}</div>
                      <ChartCore data={data} {...props} />
                    </div>
                  )}
                </AgentChartFetcher>
              </React.Suspense>
            </ErrorBoundary>
          ))}
        </div>
      )}
      {isBelowXl && (
        <div className="h-full">
          <Tabs defaultValue={chartIdList[0]} className="h-full">
            <TabsList>
              {chartIdList.map((id) => (
                <TabsTrigger key={id} value={id} className="text-xs">
                  {id}
                </TabsTrigger>
              ))}
            </TabsList>
            {chartIdList.map((chartId) => (
              <ErrorBoundary key={chartId}>
                <React.Suspense
                  fallback={
                    <ChartSkeleton skeletonOption={{ viewBoxWidth: 800, viewBoxHeight: 450 }} />
                  }
                >
                  <AgentChartFetcher
                    fromDate={fromDate}
                    toDate={toDate}
                    chartId={chartId}
                    agentId={transactionInfo.agentId}
                  >
                    {({ data, ...props }) => (
                      <TabsContent
                        key={chartId}
                        value={chartId}
                        className="relative h-[calc(100%-2.5rem)]"
                      >
                        <div className="absolute w-full text-xs text-center">{data.title}</div>
                        <ChartCore data={data} {...props} />
                      </TabsContent>
                    )}
                  </AgentChartFetcher>
                </React.Suspense>
              </ErrorBoundary>
            ))}
          </Tabs>
        </div>
      )}
    </div>
  );
};
