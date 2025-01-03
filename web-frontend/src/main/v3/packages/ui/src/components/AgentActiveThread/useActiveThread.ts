import React from 'react';
import { AgentActiveThread } from '@pinpoint-fe/ui/constants';
import { ScatterDataType } from '@pinpoint-fe/scatter-chart';

const getType = (index: number) => {
  if (index === 0) {
    return '1s';
  } else if (index === 1) {
    return '3s';
  } else if (index === 2) {
    return '5s';
  } else if (index === 3) {
    return 'slow';
  }
};

export interface ModifiedActiveThreadStatus extends Partial<AgentActiveThread.ActiveThreadStatus> {
  lastTimestamp?: number;
  scatterData?: ScatterDataType[];
}

export interface ModifiedActiveTotalThreadStatus extends ModifiedActiveThreadStatus {
  status: number[];
}

export interface ActiveThradStatusWithTotal {
  [key: string]: ModifiedActiveThreadStatus;
  __total__: ModifiedActiveTotalThreadStatus;
}

export const useActiveThread = () => {
  const prevActiveThreadCountsWithTotal = React.useRef<ActiveThradStatusWithTotal>();
  const [activeThreadCounts, setActiveThreadCounts] = React.useState<AgentActiveThread.Response>();
  const [activeThreadCountsWithTotal, setActiveThreadCountsWithTotal] =
    React.useState<ActiveThradStatusWithTotal>();

  React.useEffect(() => {
    const prevThreadCounts = prevActiveThreadCountsWithTotal.current;

    const newTimestamp = activeThreadCounts?.result?.timeStamp;
    const nweThreadCounts = activeThreadCounts?.result?.activeThreadCounts || {};
    const newThreadKeys = Object.keys(nweThreadCounts);

    const threads = newThreadKeys.reduce((acc, key) => {
      // curr
      const thread = nweThreadCounts[key];
      const lastTimestamp =
        // code: -1 => message: 'TIMEOUT'
        // code: 0 => message 'OK'
        thread.code === -1
          ? prevThreadCounts?.[key]?.lastTimestamp
            ? prevThreadCounts?.[key]?.lastTimestamp
            : newTimestamp
          : newTimestamp;

      return {
        ...acc,
        [key]: {
          ...thread,
          lastTimestamp,
          scatterData: thread?.status?.map((stat, i) => {
            return {
              x: newTimestamp,
              y: stat,
              type: getType(i),
            };
          }),
        },
      };
    }, {});

    const totalStatus = Object.values(nweThreadCounts).reduce<number[]>((acc, curr) => {
      curr.status?.forEach((stat, i) => {
        if (acc[i]) {
          acc[i] = acc[i] + stat;
        } else {
          acc[i] = stat;
        }
      });
      return acc;
    }, []);

    const threadWithTotal = {
      ...threads,
      __total__: {
        lastTimestamp: threads ? newTimestamp : undefined,
        status: totalStatus,
        scatterData: totalStatus.map((stat, i) => ({
          x: newTimestamp,
          y: stat,
          type: getType(i),
        })),
      },
    };

    if (prevActiveThreadCountsWithTotal.current) {
      prevActiveThreadCountsWithTotal.current = threadWithTotal as ActiveThradStatusWithTotal;
    }
    setActiveThreadCountsWithTotal(threadWithTotal as ActiveThradStatusWithTotal);
  }, [activeThreadCounts]);

  return { activeThreadCountsWithTotal, setActiveThreadCounts };
};
