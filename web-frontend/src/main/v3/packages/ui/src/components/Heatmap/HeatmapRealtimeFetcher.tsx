import React from 'react';
import HeatmapChartCore, { HeatmapChartCoreProps } from './core/HeatmapChartCore';
import { useStoragedSetting, useGetHeatmapAppData } from '@pinpoint-fe/ui/src/hooks';
import {
  APP_SETTING_KEYS,
  ApplicationType,
  GetHeatmapAppData,
  GetServerMap,
} from '@pinpoint-fe/ui/src/constants';
import { subMinutes, subSeconds, addSeconds } from 'date-fns';

const DefaultAxisY = [0, 10000];

// (from 계산을 위해) 가장 가까운 10초 단위로 올림한 뒤에 30초를 빼는 함수
function ceilTo10SecAndSubtract30(date: Date): Date {
  const seconds = date.getSeconds();
  const remainder = seconds % 10;
  const toAdd = remainder === 0 ? 0 : 10 - remainder;

  const roundedUp = addSeconds(date, toAdd);
  const result = subSeconds(roundedUp, 30);

  // milliseconds 제거
  result.setMilliseconds(0);
  return result;
}

export type HeatmapRealtimeFetcherProps = {
  nodeData: GetServerMap.NodeData | ApplicationType;
  agentId?: string;
} & Pick<HeatmapChartCoreProps, 'toolbarOption'>;

export const HeatmapRealtimeFetcher = ({
  nodeData,
  agentId,
  ...props
}: HeatmapRealtimeFetcherProps) => {
  const now = new Date();
  const lastToTimestamp = React.useRef<number>(); // 마지막 호출 시간을 기억해서 from~to 보다 전이면 "lastToDate ~ new Date()" 로 호출
  const [realtimeDateRange, setRealtimeDateRange] = React.useState({
    from: subMinutes(now, 5),
    to: now,
  });
  const [realtimeData, setRealtimeData] = React.useState<GetHeatmapAppData.Response>();

  React.useEffect(() => {
    const interval = setInterval(() => {
      const now = new Date();
      setRealtimeDateRange({
        from: ceilTo10SecAndSubtract30(now),
        to: now,
      });
    }, 5000);

    return () => {
      clearInterval(interval);
    };
  }, []);

  React.useEffect(() => {
    const now = new Date();
    setRealtimeDateRange({
      from: subMinutes(now, 5),
      to: now,
    });
  }, [nodeData?.applicationName]);

  const [setting] = useStoragedSetting(APP_SETTING_KEYS.HEATMAP_SETTING);

  const [parameters, setParameters] = React.useState<GetHeatmapAppData.Parameters>({
    applicationName: nodeData?.applicationName,
    serviceTypeName: nodeData?.serviceType,
    from: realtimeDateRange.from.getTime(),
    to: realtimeDateRange.to.getTime(),
    minElapsedTime: Number(setting?.yMin) || DefaultAxisY[0],
    maxElapsedTime: Number(setting?.yMax) || DefaultAxisY[1],
    agentId: agentId,
  });
  const { data, isLoading } = useGetHeatmapAppData(parameters);

  React.useEffect(() => {
    setParameters({
      applicationName: nodeData?.applicationName,
      serviceTypeName: nodeData?.serviceType,
      from:
        !!lastToTimestamp.current && lastToTimestamp.current < realtimeDateRange.from.getTime()
          ? lastToTimestamp.current
          : realtimeDateRange.from.getTime(),
      to: realtimeDateRange.to.getTime(),
      minElapsedTime: Number(setting?.yMin) || DefaultAxisY[0],
      maxElapsedTime: Number(setting?.yMax) || DefaultAxisY[1],
      agentId: agentId,
    });
  }, [
    realtimeDateRange.from.getTime(),
    realtimeDateRange.to.getTime(),
    setting?.yMin,
    setting?.yMax,
    nodeData?.applicationName,
    nodeData?.serviceType,
    agentId,
  ]);

  React.useEffect(() => {
    if (!data) {
      return;
    }

    const newLastTimeStamp = data?.heatmapData?.sort((a, b) => b.timestamp - a.timestamp)?.[0]
      ?.timestamp;
    lastToTimestamp.current = newLastTimeStamp;

    setRealtimeData((prevData) => {
      if (!prevData) {
        return data;
      } else {
        let newHeatmapData: GetHeatmapAppData.HeatmapData[] = [];
        const preHeatmapData = prevData.heatmapData?.sort((a, b) => a.timestamp - b.timestamp);
        const indexOfFirstData = preHeatmapData.findIndex(
          (item) => item.timestamp === data?.heatmapData[data?.heatmapData?.length - 1].timestamp,
        );

        if (indexOfFirstData === -1) {
          newHeatmapData = preHeatmapData
            .slice(data?.size?.width)
            .concat(data?.heatmapData?.sort((a, b) => a.timestamp - b.timestamp) || []);
        } else {
          const n = preHeatmapData.length - indexOfFirstData; // 뒤에서 n번째 데이터부터 새로 받았음;
          newHeatmapData = preHeatmapData
            .slice((data?.size?.width || 3) - n, indexOfFirstData)
            .concat(data?.heatmapData?.sort((a, b) => a.timestamp - b.timestamp) || []);
        }

        const totalSuccessCount = newHeatmapData?.reduce((total, heatmapItem) => {
          const sumInCellList = heatmapItem?.cellDataList?.reduce((cellTotal, cell) => {
            return cellTotal + cell?.successCount;
          }, 0);
          return total + sumInCellList;
        }, 0);

        const totalFailCount = newHeatmapData?.reduce((total, heatmapItem) => {
          const sumInCellList = heatmapItem?.cellDataList?.reduce((cellTotal, cell) => {
            return cellTotal + cell?.failCount;
          }, 0);
          return total + sumInCellList;
        }, 0);

        return {
          ...prevData,
          heatmapData: newHeatmapData,
          summary: {
            totalSuccessCount,
            totalFailCount,
          },
        };
      }
    });
  }, [data]);

  return (
    <HeatmapChartCore
      isRealtime={true}
      isLoading={isLoading && !realtimeData}
      nodeData={nodeData}
      data={realtimeData}
      {...props}
    />
  );
};
