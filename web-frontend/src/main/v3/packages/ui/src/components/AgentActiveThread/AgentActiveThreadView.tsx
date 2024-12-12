import React from 'react';
import { ActiveThradStatusWithTotal, ModifiedActiveTotalThreadStatus } from './useActiveThread';
import { getThreadDumpPath } from '@pinpoint-fe/utils';
import { ScatterChartCore, ScatterChartHandle } from '../../components/ScatterChart/core';
import { getAreaChartOption } from '../../components/ScatterChart/core/defaultOption';
import { Button } from '../../components/ui/button';
import { Separator } from '../../components/ui/separator';
import { RequestCounter } from './RequestCounter';
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '../../components/ui/tooltip';
import { cn } from '../../lib';
import { useSearchParameters } from '@pinpoint-fe/ui/hooks';
import { RxExternalLink, RxDrawingPinFilled, RxDrawingPin } from 'react-icons/rx';
import { BASE_PATH } from '@pinpoint-fe/constants';

export interface AgentActiveThreadViewProps {
  applicationLocked?: boolean;
  applicationName?: string;
  thread?: ActiveThradStatusWithTotal;
  onClickLockButton?: () => void;
}

const useGetMaxYCoord = (totalThread?: ModifiedActiveTotalThreadStatus) => {
  const prevCountsRef = React.useRef<number[]>([]);
  const MIN_COORD = 2;
  const maxValue =
    totalThread?.status && totalThread?.status?.length > 0 ? Math.max(...totalThread.status) : 0;

  React.useEffect(() => {
    if (prevCountsRef.current.length < 5) {
      prevCountsRef.current.push(maxValue);
    } else {
      prevCountsRef.current.shift();
      prevCountsRef.current.push(maxValue);
    }
  }, [totalThread]);

  const tempYCoord = Math.max(...prevCountsRef.current) * 2;
  const maxY = tempYCoord > MIN_COORD ? tempYCoord : MIN_COORD;

  return maxY;
};

export const AgentActiveThreadView = ({
  applicationLocked,
  applicationName,
  thread,
  onClickLockButton,
}: AgentActiveThreadViewProps) => {
  const TIME_GAP = 5000;
  const PAGE_PER_AGENT = 30;
  const { __total__, ...threads } = thread || ({} as ActiveThradStatusWithTotal);
  const { application } = useSearchParameters();
  const scatterTotalRef = React.useRef<ScatterChartHandle>(null);
  const scatterThreadRefs = React.useRef<{ [key: string]: React.RefObject<ScatterChartHandle> }>(
    {},
  );
  const [currentPage, setCurrentPage] = React.useState(0);
  const toatalPage = Math.floor(Object.keys(threads).length / PAGE_PER_AGENT) + 1;
  const displayThreadKeys = Object.keys(threads || {}).slice(
    currentPage * PAGE_PER_AGENT,
    (currentPage + 1) * PAGE_PER_AGENT,
  );
  const maxYCoord = useGetMaxYCoord(__total__);

  React.useEffect(() => {
    setCurrentPage(0);
  }, [toatalPage]);

  React.useEffect(() => {
    if (__total__?.lastTimestamp && !scatterTotalRef.current?.isRealtime()) {
      scatterTotalRef.current?.startRealtime(TIME_GAP);
    }
    if (__total__?.scatterData && scatterTotalRef.current?.isRealtime()) {
      scatterTotalRef.current?.render(__total__.scatterData);
    }
  }, [__total__]);

  React.useEffect(() => {
    if (threads) {
      displayThreadKeys.forEach((key) => {
        const t = threads[key];
        if (t?.scatterData && t.code === 0 && scatterThreadRefs.current[key].current) {
          const threadScatterChart = scatterThreadRefs.current[key]?.current;
          if (threadScatterChart?.isRealtime()) {
            threadScatterChart?.render(t.scatterData);
          } else {
            threadScatterChart?.startRealtime(TIME_GAP);
          }
        }
      });
    }
  }, [thread, scatterThreadRefs.current]);

  return (
    <div className="grid grid-cols-[28rem_auto] items-start gap-5 h-full">
      <div className="sticky top-0 flex flex-col items-center p-4">
        <div className="flex flex-row items-center justify-between w-full gap-1 p-1 text-sm font-semibold truncate">
          <TooltipProvider>
            <Tooltip>
              <TooltipTrigger>
                <Button
                  className="px-3 text-lg h-7"
                  variant="ghost"
                  onClick={() => onClickLockButton?.()}
                >
                  {applicationLocked ? <RxDrawingPinFilled /> : <RxDrawingPin />}
                </Button>
              </TooltipTrigger>
              <TooltipContent side="left">
                <p>{applicationLocked ? 'Unlock current server' : 'Lock current server'}</p>
              </TooltipContent>
            </Tooltip>
          </TooltipProvider>
          <div className="w-full truncate">{applicationName}</div>
        </div>
        <div className="flex w-full">
          <div className="w-full h-52">
            {__total__ && (
              <ScatterChartCore
                x={
                  __total__.lastTimestamp
                    ? [__total__.lastTimestamp - TIME_GAP, __total__.lastTimestamp]
                    : [0, TIME_GAP]
                }
                y={[0, maxYCoord]}
                ref={scatterTotalRef}
                getOption={getAreaChartOption}
                toolbarOption={{ hide: true }}
              />
            )}
          </div>
          <RequestCounter className="self-center h-40 pl-3" thread={__total__} />
        </div>
      </div>
      <div className="h-full p-4 border-l-1">
        <div className="flex">
          <div className="flex items-center text-sm">
            Total Servers:
            <span className="ml-1 font-semibold">{Object.keys(threads || {}).length}</span>
          </div>
          <div className="ml-auto">
            {toatalPage > 1 &&
              Array.from({ length: toatalPage }).map((_, i) => (
                <Button
                  variant={currentPage === i ? 'default' : 'ghost'}
                  className="h-6 p-2"
                  onClick={() => setCurrentPage(i)}
                >
                  {i + 1}
                </Button>
              ))}
          </div>
        </div>
        <Separator className="my-2" />
        <div className="flex flex-wrap gap-2">
          {displayThreadKeys.map((key) => {
            const data = threads[key];
            if (!scatterThreadRefs.current[key]) {
              scatterThreadRefs.current = {
                ...scatterThreadRefs.current,
                [key]: React.createRef(),
              };
            }
            const hasIssue = data?.code === -1;
            const isTimeout = hasIssue && data.message === 'TIMEOUT';
            const isError = hasIssue && data.message === 'ERROR';
            const isRealtime = scatterThreadRefs.current[key]?.current?.isRealtime();

            return (
              data && (
                <div className="relative w-48 p-1 rounded" key={key}>
                  {hasIssue ? (
                    <div
                      className={cn(
                        'absolute top-0 left-0 w-full h-full z-[1] rounded flex items-center justify-center',
                        {
                          'text-destructive': isTimeout || isError,
                        },
                      )}
                    >
                      {data.message}
                    </div>
                  ) : (
                    !isRealtime && (
                      <div className="absolute top-0 left-0 w-full h-full z-[1] rounded flex items-center justify-center">
                        CONNECTING...
                      </div>
                    )
                  )}
                  <div className="flex items-center">
                    <div className="w-full p-1 text-xs truncate">{key}</div>
                    <Button
                      className="text-muted-foreground p-0 w-4 h-4 mr-1.5"
                      variant="ghost"
                      onClick={() => {
                        window.open(`${BASE_PATH}${getThreadDumpPath(application)}?agentId=${key}`);
                      }}
                    >
                      <RxExternalLink />
                    </Button>
                  </div>
                  <div className="w-full h-28">
                    <ScatterChartCore
                      x={[data.lastTimestamp! - TIME_GAP, data.lastTimestamp!]}
                      y={[0, maxYCoord]}
                      ref={scatterThreadRefs.current[key]}
                      getOption={getAreaChartOption}
                      toolbarOption={{ hide: true }}
                    />
                  </div>
                </div>
              )
            );
          })}
        </div>
      </div>
    </div>
  );
};
