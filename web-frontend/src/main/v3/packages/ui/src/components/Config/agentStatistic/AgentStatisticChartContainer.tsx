import React from 'react';
import { AgentStatisticChart, ChartData } from './AgentStatisticChart';
import { SearchApplication } from '@pinpoint-fe/ui/constants';

/* eslint-disable @typescript-eslint/no-explicit-any */
const useVersionChartData = (data: any, versionKey: 'vmVersion' | 'agentVersion') =>
  React.useMemo(() => {
    const instances = data?.flatMap((group: SearchApplication.Application) => group.instancesList);

    const versionCounts = instances?.reduce((acc: any, instance: SearchApplication.Instance) => {
      const version = instance[versionKey];
      if (version) {
        acc[version] = (acc[version] || 0) + 1;
      }
      return acc;
    }, {} as any);

    return Object.entries(versionCounts || {}).map(([version, value]) => ({
      [versionKey]: version,
      value,
    }));
  }, [data, versionKey]);

export function AgentStatisticContainer({ data }: { data?: SearchApplication.Application[] }) {
  const vmVersionChartData = useVersionChartData(data, 'vmVersion');
  const agentVersionChartData = useVersionChartData(data, 'agentVersion');

  return (
    <div className="flex min-w-full gap-5 max-h-[40%]">
      <AgentStatisticChart type="vmVersion" chartData={vmVersionChartData as ChartData[]} />
      <AgentStatisticChart type="agentVersion" chartData={agentVersionChartData as ChartData[]} />
    </div>
  );
}
