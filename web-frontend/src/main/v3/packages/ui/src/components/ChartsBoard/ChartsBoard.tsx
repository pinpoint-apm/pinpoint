import React from 'react';
import { GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { ResponseSummaryChart, ResponseAvgMaxChart, LoadChart, LoadChartProps } from '../Chart';
import { cn } from '../../lib';
import { HelpPopover, Separator } from '..';
import { colors } from '@pinpoint-fe/ui/src/constants';

export interface ChartsBoardProps {
  className?: string;
  chartsContainerClassName?: string;
  children?: React.ReactNode;
  header: React.ReactNode;
  nodeData?: GetServerMap.NodeData;
  emptyMessage?: string;
}

export const ChartsBoard = ({
  className,
  chartsContainerClassName,
  header,
  children,
  nodeData,
  emptyMessage,
}: ChartsBoardProps) => {
  const wrapperRef = React.useRef<HTMLDivElement>(null);
  const [gridMode, setGridMode] = React.useState(false);

  React.useEffect(() => {
    const wrapperElement = wrapperRef.current;
    if (!wrapperElement) return;
    const resizeObserver = new ResizeObserver(() => {
      wrapperElement.clientWidth > 650 ? setGridMode(true) : setGridMode(false);
    });
    resizeObserver.observe(wrapperElement);

    return () => {
      resizeObserver.disconnect();
    };
  }, []);

  return (
    <div className={cn('flex h-full w-full flex-col bg-white', className)}>
      {header}
      <div className={cn('w-full h-full overflow-y-auto', chartsContainerClassName)}>
        {children}
        {nodeData && (
          <div
            ref={wrapperRef}
            className={cn('grid grid-cols-[100%]', {
              'grid-cols-[50%_50%]': gridMode,
            })}
          >
            <div className="px-4 py-2.5">
              <ResponseSummaryChart
                className="h-40"
                title={<div className="flex items-center h-12 font-semibold">Response Summary</div>}
                categories={Object.keys(nodeData?.histogram || {})}
                data={(categories) => {
                  const histogram = nodeData?.histogram;
                  if (histogram) {
                    return categories?.map((category) => {
                      return histogram[category as keyof GetServerMap.Histogram] as number;
                    });
                  }
                  return [];
                }}
                emptyMessage={emptyMessage}
              />
            </div>
            {!gridMode && <Separator />}
            <div className="px-4 py-2.5">
              <ResponseAvgMaxChart
                className="h-40"
                title={
                  <div className="flex items-center h-12 font-semibold">Response Avg & Max</div>
                }
                data={(categories) => {
                  const responseStatistics = nodeData?.responseStatistics;
                  if (responseStatistics) {
                    return categories!.map((category) => {
                      return responseStatistics?.[
                        category as keyof GetServerMap.ResponseStatistics
                      ];
                    });
                  }
                  return [];
                }}
                emptyMessage={emptyMessage}
              />
            </div>
            {!gridMode && <Separator />}
            <div className="px-4 py-2.5">
              <LoadChart
                className="h-40"
                title={
                  <div className="flex items-center h-12 gap-1 font-semibold">
                    Load
                    <HelpPopover helpKey="HELP_VIEWER.LOAD" />
                  </div>
                }
                datas={(chartColors: LoadChartProps['chartColors']) => {
                  const keys = Object.keys(chartColors!);

                  return {
                    dates: nodeData?.timeSeriesHistogram?.[0]?.values?.map((v) => v[0]),
                    ...keys.reduce((prev, curr) => {
                      const matchedHistogram = nodeData?.timeSeriesHistogram?.find(
                        ({ key }: { key: string }) => key === curr,
                      );

                      return matchedHistogram
                        ? {
                            ...prev,
                            [curr]: matchedHistogram?.values?.map?.((v) => v[1]),
                          }
                        : prev;
                    }, {}),
                  };
                }}
                emptyMessage={emptyMessage}
              />
            </div>
            {!gridMode && <Separator />}
            <div className="px-4 py-2.5">
              <LoadChart
                className="h-40"
                title={<div className="flex items-center h-12 font-semibold">Load Avg & Max</div>}
                chartColors={{
                  Avg: colors.green[300],
                  Max: colors.sky[500],
                }}
                datas={{
                  dates: nodeData?.timeSeriesHistogram?.[0]?.values?.map((v) => v[0]),
                  ...['Avg', 'Max'].reduce((prev, curr) => {
                    const matchedHistogram = nodeData?.timeSeriesHistogram?.find(
                      ({ key }: { key: string }) => key === curr,
                    );

                    return {
                      ...prev,
                      [curr]: matchedHistogram?.values?.map?.((v) => v[1]),
                    };
                  }, {}),
                }}
                emptyMessage={emptyMessage}
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
