import React from 'react';
import { AgentActiveThread } from '@pinpoint-fe/ui/constants';
import { AgentActiveTable, AgentActiveData } from './AgentActiveTable';
import { AgentActiveSettingType } from './AgentActiveSetting';
import { AgentActiveChart } from './AgentActiveChart';
export interface AgentActiveThreadViewProps {
  activeThreadCounts?: AgentActiveThread.Result;
  setting?: AgentActiveSettingType;
}

export const AgentActiveThreadView = ({
  activeThreadCounts,
  setting,
}: AgentActiveThreadViewProps) => {
  const prevActiveThreadCounts = React.useRef<{ [server: string]: number[] }>({});
  const [activeThreadCountsData, setActiveThreadCountsData] = React.useState<AgentActiveData[]>(
    getActiveThreadCountsData(activeThreadCounts?.activeThreadCounts || {}),
  );
  const [clickedActiveThread, setClickedActiveThread] = React.useState<string>('');

  // React.useEffect(() => {
  //   const interval = setInterval(() => {
  //     // 새로운 activeThreadCountsData 생성 로직
  //     const newData = Array.from({ length: 30 }, (_, index) => ({
  //       server: `pd-my2-6cf95bf889-zzkls${index + 1}`,
  //       '1s': Math.floor(Math.random() * 25),
  //       '3s': Math.floor(Math.random() * 25),
  //       '5s': Math.floor(Math.random() * 25),
  //       slow: Math.floor(Math.random() * 25),
  //     }));
  //     setActiveThreadCountsData(newData);
  //   }, 1000);

  //   return () => clearInterval(interval);
  // }, []);

  React.useEffect(() => {
    setActiveThreadCountsData(
      getActiveThreadCountsData(activeThreadCounts?.activeThreadCounts || {}),
    );
  }, [activeThreadCounts]);

  function getActiveThreadCountsData(
    activeThreadCounts: AgentActiveThread.ActiveThreadCounts,
  ): AgentActiveData[] {
    return Object.keys(activeThreadCounts).map((server) => {
      const { message, status = [0, 0, 0, 0] } = activeThreadCounts[server] || {};
      if (message && message === 'OK') {
        const [oneS, threeS, fiveS, slow] = status;

        prevActiveThreadCounts.current = { ...prevActiveThreadCounts.current, [server]: status };
        return { server, '1s': oneS, '3s': threeS, '5s': fiveS, slow: slow };
      }
      const [oneS, threeS, fiveS, slow] = prevActiveThreadCounts?.current?.[server] || [0, 0, 0, 0];
      return { server, '1s': oneS, '3s': threeS, '5s': fiveS, slow: slow };
    });
  }

  return (
    <>
      {activeThreadCountsData?.length < 100 || !setting?.isSplit ? (
        <AgentActiveChart
          data={activeThreadCountsData}
          setting={setting}
          clickedActiveThread={clickedActiveThread}
          setClickedActiveThread={setClickedActiveThread}
        />
      ) : (
        <div className="flex flex-col flex-grow gap-2">
          <AgentActiveChart
            data={activeThreadCountsData?.slice(0, Math.ceil(activeThreadCountsData.length / 2))}
            setting={setting}
            clickedActiveThread={clickedActiveThread}
            setClickedActiveThread={setClickedActiveThread}
          />
          <AgentActiveChart
            data={activeThreadCountsData?.slice(Math.ceil(activeThreadCountsData.length / 2))}
            setting={setting}
            clickedActiveThread={clickedActiveThread}
            setClickedActiveThread={setClickedActiveThread}
          />
        </div>
      )}
      <AgentActiveTable data={activeThreadCountsData} clickedActiveThread={clickedActiveThread} />
    </>
  );
};
