import React from 'react';
import { SCATTER_DATA_TOTAL_KEY, BASE_PATH } from '@pinpoint-fe/constants';
import { CurrentTarget } from '@pinpoint-fe/ui/atoms';
import {
  convertParamsToQueryString,
  getScatterFullScreenPath,
  getTransactionListPath,
  getTranscationListQueryString,
} from '@pinpoint-fe/ui/utils';
import { useGetScatterData, useServerMapSearchParameters } from '@pinpoint-fe/ui/hooks';
import { scatterDataAtom } from '@pinpoint-fe/ui/atoms';
import { useAtom } from 'jotai';
import { ScatterChartCore, ScatterChartCoreProps, ScatterChartHandle } from './core';
import { useStoragedAxisY } from './core/useStoragedAxisY';

export interface ScatterChartFetcherProps {
  node: CurrentTarget;
  agentId?: string;
  toolbarOption?: ScatterChartCoreProps['toolbarOption'];
}

export const ScatterChartFetcher = ({
  node,
  agentId = SCATTER_DATA_TOTAL_KEY,
  toolbarOption,
}: ScatterChartFetcherProps) => {
  const scatterRef = React.useRef<ScatterChartHandle>(null);
  const { dateRange, searchParameters } = useServerMapSearchParameters();
  const from = dateRange.from.getTime();
  const to = dateRange.to.getTime();
  const currentNode = `${node.applicationName}^${node.serviceType}`;
  const [x, setX] = React.useState<[number, number]>([from, to]);
  const [y, setY] = useStoragedAxisY();
  const isScatterMounted = scatterRef.current?.isMounted();
  const { data, isLoading, setQueryParams } = useGetScatterData(node);
  const [scatterData, setScatterData] = useAtom(scatterDataAtom);

  React.useEffect(() => {
    if (isScatterMounted) {
      scatterRef.current?.clear();
      setScatterData(undefined);
      setX([from, to]);
    }
  }, [currentNode, isScatterMounted, from, to]);

  React.useEffect(() => {
    const sc = scatterRef.current;
    if (sc?.getChartSize()) {
      const { width, height } = sc.getChartSize();
      setQueryParams((prev) => ({
        ...prev,
        from,
        to,
        xGroupUnit: Math.round((x[1] - x[0]) / width),
        yGroupUnit: Math.round((y[1] - y[0]) / height) || 1,
        timestamp: new Date().getTime(),
      }));
    }
  }, [isScatterMounted, from, to, x, y]);

  React.useEffect(() => {
    if (!isLoading && data) {
      setScatterData(data);
      scatterRef?.current?.render(scatterData.curr[agentId] || []);
    }
  }, [data, isLoading]);

  const handleApplyAxisSetting = ({ yMin, yMax }: { yMin: number; yMax: number }) => {
    setY([yMin, yMax]);
  };

  return (
    <ScatterChartCore
      x={x}
      y={y}
      ref={scatterRef}
      resizable={true}
      className="relative"
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
              `${BASE_PATH}${getScatterFullScreenPath(node)}?${convertParamsToQueryString({
                from: searchParameters.from,
                to: searchParameters.to,
              })}`,
              '_blank',
            );
          },
        },
        ...toolbarOption,
      }}
      onDragEnd={(data, checkedLegends) => {
        window.open(
          `${BASE_PATH}${getTransactionListPath(
            node,
            searchParameters,
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
