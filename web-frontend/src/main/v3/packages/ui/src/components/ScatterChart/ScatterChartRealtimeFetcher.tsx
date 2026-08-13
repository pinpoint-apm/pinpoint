import React from 'react';
import { SCATTER_DATA_TOTAL_KEY, BASE_PATH, GetScatter } from '@pinpoint-fe/ui/src/constants';
import { CurrentTarget } from '@pinpoint-fe/ui/src/atoms';
import {
  convertParamsToQueryString,
  getFormattedDateRange,
  getScatterData,
  getScatterFullScreenRealtimePath,
  getTransactionListPath,
  getTransactionListQueryString,
} from '@pinpoint-fe/ui/src/utils';
import {
  useGetScatterData,
  useGetScatterRealtimeData,
  useServerMapSearchParameters,
  useServiceNameForLink,
} from '@pinpoint-fe/ui/src/hooks';
import { ScatterChartCore, ScatterChartCoreProps, ScatterChartHandle } from './core';
import { useStoragedAxisY } from './core/useStoragedAxisY';
import { subMinutes, subSeconds } from 'date-fns';

/**
 * 실시간 꼬리를 아직 요청하지 않을 때 넘기는 빈 구간.
 *
 * `useGetScatterRealtimeData`는 from/to가 0이면 요청하지 않는다. 렌더마다 새로 만들지 않도록
 * 모듈 상수로 둔다(훅 내부 effect가 from/to를 의존성으로 본다).
 */
const EMPTY_RANGE = { from: new Date(0), to: new Date(0), isRealtime: true };

export interface ScatterChartRealtimeFetcherProps {
  node: CurrentTarget;
  agentId?: string;
  toolbarOption?: ScatterChartCoreProps['toolbarOption'];
}

export const ScatterChartRealtimeFetcher = ({
  node,
  agentId = SCATTER_DATA_TOTAL_KEY,
  toolbarOption,
}: ScatterChartRealtimeFetcherProps) => {
  const scatterRef = React.useRef<ScatterChartHandle>(null);
  const { dateRange } = useServerMapSearchParameters();
  const serviceNameForLink = useServiceNameForLink();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const currentNode = `${node.applicationName}^${node.serviceType}`;
  const [x] = React.useState<[number, number]>([from, to]);
  const [y, setY] = useStoragedAxisY();

  // 5분 전 ~ 현재까지의 데이터
  const [defaultDateRange, setDefaultDateRange] = React.useState(dateRange);
  const { data, isLoading, setQueryParams } = useGetScatterData(node, defaultDateRange);

  // 5분 밑그림이 덮은 끝 시각. 마운트 직후의 2초 구간은 이미 이 안에 들어 있어서, 그대로 요청하면
  // 같은 구간을 한 번 더 받는다. 시계가 이 시각을 지난 다음부터 꼬리를 물기 시작한다.
  const [backfilledTo, setBackfilledTo] = React.useState(0);
  const hasTailToFetch = backfilledTo > 0 && to > backfilledTo;

  const {
    data: realtimeData,
    isLoading: isRealtimeLoading,
    setQueryParams: setRealtimeQueryParams,
  } = useGetScatterRealtimeData(
    node,
    hasTailToFetch ? { ...dateRange, from: subSeconds(dateRange.to, 2) } : EMPTY_RANGE,
  );
  const sc = scatterRef.current;
  const isScatterMounted = scatterRef.current?.isMounted();

  React.useEffect(() => {
    const now = new Date();
    setDefaultDateRange({
      from: subMinutes(now, 5),
      to: now,
      isRealtime: true,
    });
  }, [node?.applicationName]);

  React.useEffect(() => {
    if (!isLoading && data) {
      const scatterData = getScatterData(data);
      sc?.render(scatterData.curr?.[agentId] || []);
    }
  }, [data]);

  React.useEffect(() => {
    if (!isRealtimeLoading && realtimeData) {
      const scatterData = getScatterData(realtimeData);
      sc?.render(scatterData.curr?.[agentId] || []);
    }
  }, [realtimeData]);

  React.useEffect(() => {
    if (sc && isScatterMounted) {
      const { width, height } = sc.getChartSize();

      if (sc.isRealtime()) {
        sc.stopRealtime();
      }

      sc.clear();
      sc.setAxisOption({ x: { min: from, max: to }, y: { min: y[0], max: y[1] } });
      sc.startRealtime(to - from);

      const xGroupUnit = Math.round((x[1] - x[0]) / width);
      const yGroupUnit = Math.round((y[1] - y[0]) / height) || 1;
      const timestamp = new Date().getTime();

      setQueryParams((prev: GetScatter.Parameters) => ({
        ...prev,
        from: from,
        to: to,
        xGroupUnit,
        yGroupUnit,
        timestamp,
      }));
      setBackfilledTo(to);

      setRealtimeQueryParams((prev: GetScatter.Parameters) => ({
        ...prev,
        xGroupUnit,
        yGroupUnit,
        timestamp,
      }));
    }
  }, [isScatterMounted, x, y, currentNode]);

  const handleApplyAxisSetting = ({ yMin, yMax }: { yMin: number; yMax: number }) => {
    setY([yMin, yMax]);
  };

  return (
    <ScatterChartCore
      x={x}
      y={y}
      ref={scatterRef}
      resizable={true}
      toolbarOption={{
        captureImage: {
          fileName: `Pinpoint_Scatter_Chart__${currentNode || ''}`,
        },
        axisSetting: {
          onApply: handleApplyAxisSetting,
        },
        expand: {
          onClick: () => {
            window.open(
              `${BASE_PATH}${getScatterFullScreenRealtimePath(node)}${
                agentId === SCATTER_DATA_TOTAL_KEY
                  ? ''
                  : '?' + convertParamsToQueryString({ agentId })
              }`,
              '_blank',
            );
          },
        },
        ...toolbarOption,
      }}
      onDragEnd={(data, checkedLegends) => {
        window.open(
          `${getTransactionListPath(
            node,
            getFormattedDateRange(dateRange),
            serviceNameForLink,
          )}&${getTransactionListQueryString({
            ...data,
            checkedLegends,
            agentId: agentId === SCATTER_DATA_TOTAL_KEY ? '' : agentId,
          })}`,
        );
      }}
    />
  );
};
