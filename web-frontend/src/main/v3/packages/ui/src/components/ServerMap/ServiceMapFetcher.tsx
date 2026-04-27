import React from 'react';
import { useSetAtom } from 'jotai';
import { MergedEdge, MergedNode } from '@pinpoint-fe/server-map';
import {
  serverMapDataAtom,
  currentServerAtom,
  serverMapCurrentTargetAtom,
} from '@pinpoint-fe/ui/src/atoms';
import {
  useExperimentals,
  useGetServiceMapData,
  useServerMapSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { GetServerMap, GetServiceMap } from '@pinpoint-fe/ui/src/constants';
import { useTranslation } from 'react-i18next';
import { getServiceMapBaseNodeId, getServerImagePath } from '@pinpoint-fe/ui/src/utils';
import { ServerMapCore, ServerMapCoreProps } from './ServerMapCore';

export type ServiceMapFetcherProps = Pick<
  ServerMapCoreProps,
  'onClickMenuItem' | 'onApplyChangedOption' | 'queryOption'
>;

export const ServiceMapFetcher = ({ ...props }: ServiceMapFetcherProps) => {
  const setDataAtom = useSetAtom(serverMapDataAtom);
  const setCurrentServer = useSetAtom(currentServerAtom);
  const setServerMapCurrentTarget = useSetAtom(serverMapCurrentTargetAtom);
  const { application, dateRange, queryOption } = useServerMapSearchParameters();
  const experimentalOption = useExperimentals();
  const useStatisticsAgentState = experimentalOption.statisticsAgentState.value || true;

  const { data, isLoading, error } = useGetServiceMapData({
    applicationName: application?.applicationName,
    serviceTypeName: application?.serviceType,
    from: dateRange.from.getTime(),
    to: dateRange.to.getTime(),
    calleeRange: queryOption.inbound,
    callerRange: queryOption.outbound,
    wasOnly: !!queryOption.wasOnly,
    bidirectional: !!queryOption.bidirectional,
    useStatisticsAgentState,
  });
  const { t } = useTranslation();

  // GetServiceMap.Response shares the same runtime shape for server-map rendering
  React.useEffect(() => {
    setDataAtom(data as unknown as GetServerMap.Response);
  }, [data]);

  const applicationMapData = data?.applicationMapData;
  const baseNodeId = getServiceMapBaseNodeId({ application, applicationMapData });

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

  const handleMergeStateChange = () => {
    if (data && baseNodeId) {
      const baseNode = applicationMapData?.nodeDataArray.find(
        (node): node is GetServiceMap.NodeData =>
          !GetServiceMap.isServiceGroupNode(node) && node.key === baseNodeId,
      );
      if (baseNode) {
        setServerMapCurrentTarget({
          id: baseNode.key,
          applicationName: baseNode.applicationName,
          serviceType: baseNode.serviceType,
          imgPath: getServerImagePath(baseNode),
          type: 'node',
        });
      }
    }
  };

  return (
    <ServerMapCore
      data={(data as unknown as GetServerMap.Response) || {}}
      isLoading={isLoading}
      error={error}
      forceLayoutUpdate={true}
      onClickNode={handleClickNode}
      onClickEdge={handleClickEdge}
      onMergeStateChange={handleMergeStateChange}
      baseNodeId={baseNodeId}
      inputPlaceHolder={t('COMMON.SEARCH_INPUT')}
      {...props}
    />
  );
};
