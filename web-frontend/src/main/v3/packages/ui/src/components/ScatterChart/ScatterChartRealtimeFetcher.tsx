import React from 'react';
import { SCATTER_DATA_TOTAL_KEY, BASE_PATH, GetScatter } from '@pinpoint-fe/constants';
import { CurrentTarget } from '@pinpoint-fe/ui/atoms';
import {
  convertParamsToQueryString,
  getFormattedDateRange,
  getScatterData,
  getScatterFullScreenRealtimePath,
  getTransactionListPath,
  getTranscationListQueryString,
} from '@pinpoint-fe/ui/utils';
import { useGetScatterRealtimeData, useServerMapSearchParameters } from '@pinpoint-fe/ui/hooks';
import { ScatterChartCore, ScatterChartCoreProps, ScatterChartHandle } from './core';
import { useStoragedAxisY } from './core/useStoragedAxisY';

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
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const currentNode = `${node.applicationName}^${node.serviceType}`;
  const [x] = React.useState<[number, number]>([from, to]);
  const [y, setY] = useStoragedAxisY();
  const { data, isLoading, setQueryParams } = useGetScatterRealtimeData(node);
  const sc = scatterRef.current;
  const isScatterMounted = scatterRef.current?.isMounted();

  React.useEffect(() => {
    if (!isLoading && data) {
      const scatterData = getScatterData(data);

      sc?.render(scatterData.curr?.[agentId] || []);
    }
  }, [data]);

  React.useEffect(() => {
    if (sc && isScatterMounted) {
      const { width, height } = sc.getChartSize();

      sc.isRealtime() && sc.stopRealtime();
      sc.clear();
      sc.setAxisOption({ x: { min: from, max: to }, y: { min: y[0], max: y[1] } });
      sc.startRealtime(to - from);

      setQueryParams((prev: GetScatter.Parameters) => ({
        ...prev,
        from: from,
        to: to,
        xGroupUnit: Math.round((x[1] - x[0]) / width),
        yGroupUnit: Math.round((y[1] - y[0]) / height) || 1,
        timestamp: new Date().getTime(),
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
          )}&${getTranscationListQueryString({
            ...data,
            checkedLegends,
            agentId: agentId === SCATTER_DATA_TOTAL_KEY ? '' : agentId,
          })}`,
        );
      }}
    />
  );
};
