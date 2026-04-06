import React from 'react';
import { FilteredMapType as FilteredMap, Configuration } from '@pinpoint-fe/ui/src/constants';
import { useAtom, useSetAtom } from 'jotai';
import {
  serverMapDataAtom,
  currentServerAtom,
  serverMapCurrentTargetAtom,
  scatterDataByApplicationKeyAtom,
} from '@pinpoint-fe/ui/src/atoms';
import {
  mergeFilteredMapNodeData,
  mergeFilteredMapLinkData,
  getServerImagePath,
  getBaseNodeId,
  parseNodeKey,
} from '@pinpoint-fe/ui/src/utils';
import { MergedNode, MergedEdge, Node, Edge } from '@pinpoint-fe/server-map';
import { useFilteredMapParameters, useGetFilteredServerMapData } from '@pinpoint-fe/ui/src/hooks';
import { useTranslation } from 'react-i18next';
import { SERVERMAP_MENU_FUNCTION_TYPE, ServerMapCore, ServerMapCoreProps } from '..';

export interface FilteredMapFetcherProps {
  isPaused?: boolean;
  onClickMenuItem?: ServerMapCoreProps['onClickMenuItem'];
  configuration?: Configuration;
}

export const FilteredMapFetcher = ({
  isPaused = false,
  onClickMenuItem,
  configuration,
}: FilteredMapFetcherProps) => {
  const [serverMapData, setServerMapData] = useAtom(serverMapDataAtom);
  const setCurrentServer = useSetAtom(currentServerAtom);
  const setScatterDataByApplicationKey = useSetAtom(scatterDataByApplicationKeyAtom);
  const setServerMapCurrentTarget = useSetAtom(serverMapCurrentTargetAtom);
  const { dateRange, application } = useFilteredMapParameters();
  const enableServiceMap = configuration?.['experimental.enableServiceMap.value'] ?? false;
  const from = dateRange.from.getTime();
  const { data, error, isLoading, setQueryParams } = useGetFilteredServerMapData(isPaused);
  const { t } = useTranslation();

  React.useEffect(() => {
    if (!isLoading && data) {
      setServerMapData((prev) => {
        if (prev) {
          const timestampArray = [...prev.applicationMapData.timestamp];
          const nodeDataArray = [
            ...prev.applicationMapData.nodeDataArray,
          ] as FilteredMap.NodeData[];
          const linkDataArray = [
            ...prev.applicationMapData.linkDataArray,
          ] as FilteredMap.LinkData[];

          data?.applicationMapData?.timestamp?.forEach((timestamp) => {
            if (!timestampArray.includes(timestamp)) {
              timestampArray.push(timestamp);
            }
          });

          data?.applicationMapData?.nodeDataArray?.forEach((newNodeData) => {
            const existingIndex = nodeDataArray.findIndex(
              (prevNodeData) => newNodeData.key === prevNodeData.key,
            );

            if (existingIndex !== -1) {
              nodeDataArray[existingIndex] = mergeFilteredMapNodeData(
                {
                  timestamp: prev?.applicationMapData?.timestamp,
                  data: nodeDataArray[existingIndex],
                },
                {
                  timestamp: data?.applicationMapData?.timestamp,
                  data: newNodeData,
                },
              );
            } else {
              nodeDataArray.push(newNodeData);
            }
          });

          data?.applicationMapData?.linkDataArray?.forEach((newLinkData) => {
            const existingIndex = linkDataArray.findIndex(
              (prevLinkData) => prevLinkData.key === newLinkData.key,
            );

            if (existingIndex !== -1) {
              linkDataArray[existingIndex] = mergeFilteredMapLinkData(
                {
                  timestamp: prev?.applicationMapData?.timestamp,
                  data: linkDataArray[existingIndex],
                },
                {
                  timestamp: data?.applicationMapData?.timestamp,
                  data: newLinkData,
                },
              );
            } else {
              linkDataArray.push(newLinkData);
            }
          });

          return {
            ...data,
            applicationMapData: {
              ...data?.applicationMapData,
              timestamp: timestampArray,
              nodeDataArray,
              linkDataArray,
            },
          };
        }
        return data;
      });

      setScatterDataByApplicationKey(data.applicationScatterData);

      if (data?.lastFetchedTimestamp > from) {
        setQueryParams((prev) => ({
          ...prev,
          to: data.lastFetchedTimestamp - 1,
        }));
      }
    }
  }, [data]);

  const handleClickNode: ServerMapCoreProps['onClickNode'] = ({ data, eventType }) => {
    const { label, type, imgPath, id, nodes } = data as MergedNode;
    if (eventType === 'left' || eventType === 'programmatic') {
      setServerMapCurrentTarget({
        id,
        applicationName: label,
        serviceType: type,
        imgPath: imgPath!,
        nodes,
        type: 'node',
      });
      setCurrentServer(undefined);
    }
  };

  const handleClickEdge: ServerMapCoreProps['onClickEdge'] = ({ data, eventType }) => {
    const { id, source, target, edges } = data as MergedEdge;
    if (eventType === 'left') {
      setServerMapCurrentTarget({
        id,
        source,
        target,
        edges,
        type: 'edge',
      });

      setCurrentServer(undefined);
    }
  };

  const handleClickServerMapMenuItem = (type: SERVERMAP_MENU_FUNCTION_TYPE, data: Node | Edge) => {
    onClickMenuItem?.(type, data);
  };

  const handleMergeStateChange = () => {
    const nodeKey = getBaseNodeId({
      application,
      applicationMapData: serverMapData?.applicationMapData,
      enableServiceMap,
    });
    const { applicationName, serviceType } = parseNodeKey(nodeKey);

    setServerMapCurrentTarget({
      id: nodeKey,
      applicationName,
      serviceType,
      imgPath: getServerImagePath({ applicationName, serviceType }),
      type: 'node',
    });
  };

  return (
    <ServerMapCore
      data={serverMapData}
      error={error}
      onClickNode={handleClickNode}
      onClickEdge={handleClickEdge}
      onClickMenuItem={handleClickServerMapMenuItem}
      onMergeStateChange={handleMergeStateChange}
      baseNodeId={getBaseNodeId({
        application,
        // serverMapData와 동일한 누적 데이터 기준으로 baseNodeId를 계산해야
        // ServerMapCore 렌더 데이터와 일치하여 그래프 위치가 안정적으로 유지됨
        applicationMapData: serverMapData?.applicationMapData,
        enableServiceMap,
      })}
      inputPlaceHolder={t('COMMON.SEARCH_INPUT')}
    />
  );
};
