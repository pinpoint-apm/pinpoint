import React from 'react';
import { FaThumbtack } from 'react-icons/fa';
import { ActiveThradStatusWithTotal } from './useActiveThread';
import {
  // getSortedKeys,
  getDifferentKeys,
} from '@pinpoint-fe/utils';
import { ScatterChart as SC } from '@pinpoint-fe/scatter-chart';
// import { getAreaChartOption } from '../../components/ScatterChart/core/defaultOption';
// import { ScatterChartCore } from '../../components/ScatterChart/core';

export interface AgentActiveThreadViewProps {
  applicationName?: string;
  thread?: ActiveThradStatusWithTotal;
}

export const AgentActiveThreadView = ({ applicationName, thread }: AgentActiveThreadViewProps) => {
  const TIME_GAP = 5000;
  // const { __total__, ...rest } = thread || {};
  const prevThread = React.useRef<ActiveThradStatusWithTotal>({} as ActiveThradStatusWithTotal);
  // const [scatterKeys, setScatterKeys] = React.useState<string[]>([]);
  const totalWrapperRef = React.useRef(null);
  const scatterTotalRef = React.useRef<SC & { isMounted: () => void }>();
  const scatterRefs = React.useRef<{ [key: string]: React.RefObject<SC> }>({});
  // const scatterCharts = React.useRef();
  // const currentDateRange = React.useMemo<[number, number]>(() => {
  //   const current = new Date().getTime();
  //   return [current - TIME_GAP - 1000, current - 1000];
  // }, []);

  // React.useEffect(() => {
  //   if (scatterTotalRef.current?.isMounted()) {
  //     // scatterTotalRef.current?.startRealtime(TIME_GAP);
  //   }
  // }, [scatterTotalRef.current]);

  // React.useEffect(() => {
  //   const diffKeys = getDifferentKeys(prevThread.current, scatterRefs.current);
  //   if (diffKeys.length > 0) {
  //     diffKeys.forEach((key) => {
  //       scatterRefs.current[key].current?.startRealtime(TIME_GAP);
  //     });
  //   }
  //   if (thread) {
  //     prevThread.current = thread;
  //   }
  // }, [scatterRefs.current]);

  React.useEffect(() => {
    if (thread) {
      // total
      if (thread.__total__.lastTimestamp) {
        scatterTotalRef.current?.startRealtime(TIME_GAP);
      }

      // threads
      const diffKeys = getDifferentKeys(prevThread.current, scatterRefs.current);
      if (diffKeys.length > 0) {
        diffKeys.forEach((key) => {
          scatterRefs.current[key].current?.startRealtime(TIME_GAP);
        });
      }
      prevThread.current = thread;

      Object.keys(thread).forEach((key: string) => {
        const t = thread[key];
        if (t.scatterData) {
          if (key === '__total__' && scatterTotalRef.current) {
            scatterTotalRef.current.render(t.scatterData);
          } else if (t.code === 0 && scatterRefs.current[key].current) {
            scatterRefs.current[key].current?.render(t.scatterData);
          }
        }
      });
    }
  }, [thread]);

  return (
    <div className="flex items-start gap-5 p-4">
      <div className="flex flex-col items-center p-1 font-semibold">
        <div className="flex items-center p-1">
          {applicationName}
          <div className="ml-2.5">
            <FaThumbtack fill={'var(--primary)'} />
          </div>
        </div>
        <div className="w-full h-52" ref={totalWrapperRef}>
          {/* {thread?.__total__ && (
            <ScatterChartCore
              x={
                thread.__total__.lastTimestamp
                  ? [thread.__total__.lastTimestamp - TIME_GAP, thread.__total__.lastTimestamp]
                  : [0, TIME_GAP]
              }
              y={[0, 10]}
              ref={scatterTotalRef}
              getOption={getAreaChartOption}
              toolbarOption={{ hide: true }}
            />
          )} */}
        </div>
      </div>
      <div className="flex flex-wrap gap-2.5">
        {/* {getSortedKeys(rest).map((key) => {
          const data = rest[key];
          if (!scatterRefs.current[key]) {
            scatterRefs.current = {
              ...scatterRefs.current,
              [key]: React.createRef(),
            };
          }
          const isRealtime = scatterRefs.current[key]?.current?.isRealtime();
          const isNotFound = isRealtime && data?.code === -1;

          return (
            data && (
              <div className="relative p-1 rounded w-52" key={key}>
                {!isRealtime && (
                  <div className="absolute top-0 left-0 w-full h-full z-[1] rounded flex items-center justify-center">
                    Connecting...
                  </div>
                )}
                {isNotFound && (
                  <div className="absolute top-0 left-0 w-full h-full z-[1] rounded flex items-center justify-center">
                    NOT FOUND
                  </div>
                )}
                <div className="p-1 text-xs">{key}</div>
                <div className="w-full h-32">
                  <ScatterChartCore
                    x={[data.lastTimestamp! - TIME_GAP, data.lastTimestamp!]}
                    y={[0, 10]}
                    ref={scatterRefs.current[key]}
                    getOption={getAreaChartOption}
                    toolbarOption={{ hide: true }}
                  />
                </div>
              </div>
            )
          );
        })} */}
      </div>
    </div>
  );
};
