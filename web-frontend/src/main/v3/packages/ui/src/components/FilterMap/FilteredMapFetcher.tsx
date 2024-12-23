import React from 'react';
import { FilteredMap } from '@pinpoint-fe/constants';
import { useAtom, useSetAtom } from 'jotai';
import {
  serverMapDataAtom,
  currentServerAtom,
  serverMapCurrentTargetAtom,
  scatterDataByApplicationKeyAtom,
} from '@pinpoint-fe/ui/atoms';
import {
  mergeFilteredMapNodeData,
  mergeFilteredMapLinkData,
  getServerImagePath,
  getBaseNodeId,
} from '@pinpoint-fe/utils';
import { MergedNode, MergedEdge, Node, Edge } from '@pinpoint-fe/server-map';
import { useFilteredMapParameters, useGetFilteredServerMapData } from '@pinpoint-fe/ui/hooks';
import { useTranslation } from 'react-i18next';
import { SERVERMAP_MENU_FUNCTION_TYPE, ServerMapCore, ServerMapCoreProps } from '..';

export interface FilteredMapFetcherProps {
  isPaused?: boolean;
  onClickMenuItem?: ServerMapCoreProps['onClickMenuItem'];
}

export const FilteredMapFetcher = ({
  isPaused = false,
  onClickMenuItem,
}: FilteredMapFetcherProps) => {
  // const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom);
  const [serverMapData, setServerMapData] = useAtom(serverMapDataAtom);
  const setCurrentServer = useSetAtom(currentServerAtom);
  const setScatterDataByApplicationKey = useSetAtom(scatterDataByApplicationKeyAtom);
  const setServerMapCurrentTarget = useSetAtom(serverMapCurrentTargetAtom);
  const { dateRange, application } = useFilteredMapParameters();
  const from = dateRange.from.getTime();
  const { data, isLoading, setQueryParams } = useGetFilteredServerMapData(isPaused);
  const { t } = useTranslation();

  React.useEffect(() => {
    if (!isLoading && data) {
      setServerMapData((prev) => {
        if (prev) {
          const nodeDataArray = prev.applicationMapData.nodeDataArray as FilteredMap.NodeData[];
          const linkDataArray = prev.applicationMapData.linkDataArray as FilteredMap.LinkData[];

          data.applicationMapData.nodeDataArray.forEach((newNodeData) => {
            const prevNode = nodeDataArray.find(
              (prevNodeData) => newNodeData.key === prevNodeData.key,
            );

            if (prevNode) {
              nodeDataArray.map((node) => {
                if (node.key === newNodeData.key) {
                  return mergeFilteredMapNodeData(node, newNodeData);
                }
                return node;
              });
            } else {
              nodeDataArray.push(newNodeData);
            }
          });

          data.applicationMapData.linkDataArray.forEach((newLinkData) => {
            const hasExistLink = linkDataArray.find(
              (prevLinkData) => prevLinkData.key === newLinkData.key,
            );

            if (hasExistLink) {
              linkDataArray.map((link) => {
                if (link.key === newLinkData.key) {
                  return mergeFilteredMapLinkData(link, newLinkData);
                }
                return link;
              });
            } else {
              linkDataArray.push(newLinkData);
            }
          });

          return {
            ...data,
            applicationMapData: {
              ...data.applicationMapData,
              nodeDataArray: nodeDataArray,
              linkDataArray: linkDataArray,
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
    const [applicationName, serviceType] = getBaseNodeId({
      application,
      applicationMapData: data?.applicationMapData,
    }).split('^');

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
