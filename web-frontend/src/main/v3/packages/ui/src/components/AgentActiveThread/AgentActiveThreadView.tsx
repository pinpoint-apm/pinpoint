import React from 'react';
import { AgentActiveThread } from '@pinpoint-fe/ui/src/constants';
import { AgentActiveTable, AgentActiveData } from './AgentActiveTable';
import { AgentActiveSettingType } from './AgentActiveSetting';
import { AgentActiveChart } from './AgentActiveChart';

export interface AgentActiveThreadViewProps {
  applicationName?: string;
  activeThreadCounts?: AgentActiveThread.Result;
  setting?: AgentActiveSettingType;
}

export const AgentActiveThreadView = ({
  applicationName,
  activeThreadCounts,
  setting,
}: AgentActiveThreadViewProps) => {
  const dataMap = React.useRef(
    new Map<string, { status: number[]; lastOKTimeStamp: number; message?: string }>(),
  );
  const [loading, setLoading] = React.useState<boolean>(false);
  const [activeThreadCountsData, setActiveThreadCountsData] = React.useState<AgentActiveData[]>([]);
  const [clickedActiveThread, setClickedActiveThread] = React.useState<string>('');

  React.useEffect(() => {
    if (!activeThreadCounts || activeThreadCounts.applicationName !== applicationName) {
      dataMap.current.clear();
      setClickedActiveThread('');
      setLoading(true);
      return;
    }

    setActiveThreadCountsData(getActiveThreadCountsData(activeThreadCounts));
    setLoading(false);
  }, [applicationName, activeThreadCounts]);

  function getActiveThreadCountsData({
    activeThreadCounts,
    timeStamp,
  }: AgentActiveThread.Result): AgentActiveData[] {
    if (!activeThreadCounts) {
      return [];
    }

    // Set dataMap with new activeThreadCounts
    Object.keys(activeThreadCounts || {}).forEach((key) => {
      const serverData = activeThreadCounts[key];
      if (serverData?.message === 'OK') {
        // If "OK" the status will be updated.
        dataMap.current.set(key, {
          status: serverData.status || [0, 0, 0, 0],
          lastOKTimeStamp: timeStamp,
          message: '',
        });
      } else {
        // If it is not “OK”, only the message is updated.
        const dataMapData = dataMap.current.get(key) || {
          status: [-1, -1, -1, -1],
          lastOKTimeStamp: timeStamp,
        };

        const timeDiff = timeStamp - dataMapData.lastOKTimeStamp;

        // If the time difference is greater than 3 seconds, the status is updated to -1.
        if (timeDiff > 3000) {
          dataMap.current.set(key, {
            ...dataMapData,
            status: [-1, -1, -1, -1],
            message: serverData?.message,
          });
          return;
        }

        // If the time difference is greater than the inactivityThreshold, the status is removed.
        if (timeDiff >= (setting?.inactivityThreshold || 5) * 60 * 1000) {
          dataMap.current.delete(key);
          setClickedActiveThread('');
          return;
        }

        dataMap.current.set(key, {
          ...dataMapData,
          message: serverData?.message,
        });
      }
    });

    const newData = Array.from(dataMap.current.keys()).map((server) => {
      const mapData = dataMap.current.get(server);
      const [oneS, threeS, fiveS, slow] = mapData?.status || [-1, -1, -1, -1];
      return {
        server,
        '1s': oneS,
        '3s': threeS,
        '5s': fiveS,
        slow,
        message: mapData?.message || '',
      };
    });

    return newData;
  }

  return (
    <>
      {activeThreadCountsData?.length < 100 || !setting?.isSplit ? (
        <AgentActiveChart
          loading={loading}
          data={activeThreadCountsData}
          setting={setting}
          clickedActiveThread={clickedActiveThread}
          setClickedActiveThread={setClickedActiveThread}
        />
      ) : (
        <div className="flex flex-col flex-grow gap-2">
          <AgentActiveChart
            loading={loading}
            data={activeThreadCountsData?.slice(0, Math.ceil(activeThreadCountsData.length / 2))}
            setting={setting}
            clickedActiveThread={clickedActiveThread}
            setClickedActiveThread={setClickedActiveThread}
          />
          <AgentActiveChart
            loading={loading}
            data={activeThreadCountsData?.slice(Math.ceil(activeThreadCountsData.length / 2))}
            setting={setting}
            clickedActiveThread={clickedActiveThread}
            setClickedActiveThread={setClickedActiveThread}
          />
        </div>
      )}
      <AgentActiveTable
        loading={loading}
        data={activeThreadCountsData}
        clickedActiveThread={clickedActiveThread}
      />
    </>
  );
};
