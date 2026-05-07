import React from 'react';
import {FilteredMapType as FilteredMap} from '@pinpoint-fe/ui/src/constants';
import {useAtom, useSetAtom} from 'jotai';
import {
  currentServerAtom,
  scatterDataByApplicationKeyAtom,
  serverMapCurrentTargetAtom,
  serverMapDataAtom,
} from '@pinpoint-fe/ui/src/atoms';
import {
  getBaseNodeId,
  getServerImagePath,
  mergeFilteredMapLinkData,
  mergeFilteredMapNodeData,
  parseBaseNodeId,
  toBasicISOStringMs,
} from '@pinpoint-fe/ui/src/utils';
import {Edge, MergedEdge, MergedNode, Node} from '@pinpoint-fe/server-map';
import {useFilteredMapParameters, useGetFilteredServerMapData} from '@pinpoint-fe/ui/src/hooks';
import {useTranslation} from 'react-i18next';
import {SERVERMAP_MENU_FUNCTION_TYPE, ServerMapCore, ServerMapCoreProps} from '..';

export interface FilteredMapFetcherProps {
  isPaused?: boolean;
  onClickMenuItem?: ServerMapCoreProps['onClickMenuItem'];
}

export const FilteredMapFetcher = ({
  isPaused = false,
  onClickMenuItem,
}: FilteredMapFetcherProps) => {
  const [serverMapData, setServerMapData] = useAtom(serverMapDataAtom);
  const setCurrentServer = useSetAtom(currentServerAtom);
  const setScatterDataByApplicationKey = useSetAtom(scatterDataByApplicationKeyAtom);
  const setServerMapCurrentTarget = useSetAtom(serverMapCurrentTargetAtom);
  const { dateRange, application } = useFilteredMapParameters();
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
          to: toBasicISOStringMs(new Date(data.lastFetchedTimestamp - 1)),
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
    const { applicationName, serviceType } = parseBaseNodeId(
      getBaseNodeId({
        application,
        applicationMapData: data?.applicationMapData,
      }),
    );

    setServerMapCurrentTarget({
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
        applicationMapData: data?.applicationMapData,
      })}
      inputPlaceHolder={t('COMMON.SEARCH_INPUT')}
    />
  );
};
