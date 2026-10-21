import React from 'react';
import { HeatmapChartCore, HeatmapChartCoreProps } from './core/HeatmapChartCore';
import type { HeatmapFetcherProps } from './HeatmapFetcher';
import {
  useServerMapSearchParameters,
  useStoragedSetting,
  useGetHeatmapAppData,
  useRequestService,
} from '@pinpoint-fe/ui/src/hooks';
import { APP_SETTING_KEYS, BASE_PATH, GetHeatmapAppData } from '@pinpoint-fe/ui/src/constants';
import { subMinutes, subSeconds, addSeconds } from 'date-fns';
import {
  getFormattedDateRange,
  getHeatmapFullScreenRealtimePath,
  getTransactionListPath,
  getTransactionListQueryString,
  toBasicISOString,
} from '@pinpoint-fe/ui/src/utils';

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

export interface HeatmapRealtimeFetcherProps extends HeatmapFetcherProps {}

// 실시간으로 조회한 데이터를 HeatmapChartCore 로 그린다. 설정은 저장소에, 확대·드래그는 URL로 새 창을 연다.
export const HeatmapRealtimeFetcher = ({
  nodeData,
  agentId,
  toolbarOption,
  serviceName,
}: HeatmapRealtimeFetcherProps) => {
  const now = new Date();
  const lastToTimestamp = React.useRef<number | undefined>(undefined); // 마지막 호출 시간을 기억해서 from~to 보다 전이면 "lastToDate ~ new Date()" 로 호출
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

  const [setting, setSetting] = useStoragedSetting(APP_SETTING_KEYS.HEATMAP_SETTING);
  const { dateRange } = useServerMapSearchParameters();
  // 넘어온 값이 없으면 화면의 service로 링크를 만든다(map 밖에서 쓰이는 경우).
  // 링크는 요청이 아니므로 request 에 담지 않고 최신 prop을 그대로 쓴다.
  const screenServiceName = useRequestService();
  const linkServiceName = serviceName ?? screenServiceName;

  const [request, setRequest] = React.useState<{
    parameters: GetHeatmapAppData.Parameters;
    serviceName?: string;
  }>({
    parameters: {
      applicationName: nodeData?.applicationName,
      serviceTypeName: nodeData?.serviceType,
      from: toBasicISOString(realtimeDateRange.from),
      to: toBasicISOString(realtimeDateRange.to),
      minElapsedTime: Number(setting?.yMin) || DefaultAxisY[0],
      maxElapsedTime: Number(setting?.yMax) || DefaultAxisY[1],
      agentId: agentId,
    },
    serviceName,
  });
  const { data, isLoading } = useGetHeatmapAppData(request.parameters, request.serviceName);

  React.useEffect(() => {
    setRequest({
      parameters: {
        applicationName: nodeData?.applicationName,
        serviceTypeName: nodeData?.serviceType,
        from:
          !!lastToTimestamp?.current && lastToTimestamp?.current < realtimeDateRange.from.getTime()
            ? toBasicISOString(new Date(lastToTimestamp?.current))
            : toBasicISOString(realtimeDateRange.from),
        to: toBasicISOString(realtimeDateRange.to),
        minElapsedTime: Number(setting?.yMin) || DefaultAxisY[0],
        maxElapsedTime: Number(setting?.yMax) || DefaultAxisY[1],
        agentId: agentId,
      },
      serviceName,
    });
  }, [
    realtimeDateRange.from.getTime(),
    realtimeDateRange.to.getTime(),
    setting?.yMin,
    setting?.yMax,
    nodeData?.applicationName,
    nodeData?.serviceType,
    agentId,
    serviceName,
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
        let newHeatmapData: GetHeatmapAppData.HeatmapData[];
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

  const handleExpand = () => {
    window.open(
      `${BASE_PATH}${getHeatmapFullScreenRealtimePath(nodeData, undefined, linkServiceName)}`,
      '_blank',
    );
  };

  const handleDragEnd: HeatmapChartCoreProps['onDragEnd'] = (area, checkedLegends) => {
    window.open(
      `${BASE_PATH}${getTransactionListPath(
        nodeData,
        getFormattedDateRange(dateRange),
        linkServiceName,
      )}&${getTransactionListQueryString({
        ...area,
        checkedLegends,
      })}`,
    );
  };

  return (
    <HeatmapChartCore
      isRealtime={true}
      isLoading={isLoading && !realtimeData}
      data={realtimeData}
      setting={setting}
      onSettingChange={setSetting}
      onDragEnd={handleDragEnd}
      onExpand={toolbarOption?.expand?.hide ? undefined : handleExpand}
      captureFileName={`Pinpoint_Heatmap_Chart__${nodeData?.applicationName}^${nodeData?.serviceType}.png`}
    />
  );
};
