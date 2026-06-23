import React from 'react';
import { useAtomValue, useSetAtom } from 'jotai';
import { MergedEdge, MergedNode } from '@pinpoint-fe/server-map';
import {
  serverMapDataAtom,
  currentServerAtom,
  serverMapCurrentTargetAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import {
  useExperimentals,
  useGetServiceMap,
  useServerMapSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { useTranslation } from 'react-i18next';
import {
  flattenServiceMapResponse,
  getBaseNodeId,
  getServerImagePath,
  parseBaseNodeId,
  toBasicISOString,
} from '@pinpoint-fe/ui/src/utils';
import { ServerMapCore, ServerMapCoreProps } from '../ServerMap/ServerMapCore';

export interface ServiceMapFetcherProps
  extends Pick<ServerMapCoreProps, 'onClickMenuItem' | 'onApplyChangedOption' | 'queryOption'> {
  shouldPoll?: boolean;
}

export const ServiceMapFetcher = ({ shouldPoll: _shouldPoll, ...props }: ServiceMapFetcherProps) => {
  const setDataAtom = useSetAtom(serverMapDataAtom);
  const setCurrentServer = useSetAtom(currentServerAtom);
  const setServerMapCurrentTarget = useSetAtom(serverMapCurrentTargetAtom);
  const serverMapCurrentTarget = useAtomValue(serverMapCurrentTargetAtom);
  const { application, dateRange, queryOption } = useServerMapSearchParameters();
  const experimentalOption = useExperimentals();
  const useStatisticsAgentState =
    experimentalOption[EXPERIMENTAL_CONFIG_KEYS.USE_STATISTICS_AGENT_STATE].value || true;

  const { data: rawData, isLoading, error } = useGetServiceMap({
    applicationName: application?.applicationName ?? '',
    serviceTypeName: application?.serviceType,
    from: toBasicISOString(dateRange.from),
    to: toBasicISOString(dateRange.to),
    callerRange: queryOption.outbound,
    calleeRange: queryOption.inbound,
    bidirectional: !!queryOption.bidirectional,
    wasOnly: !!queryOption.wasOnly,
    useStatisticsAgentState,
  });

  const data = React.useMemo(() => flattenServiceMapResponse(rawData), [rawData]);
  const { t } = useTranslation();

  React.useEffect(() => {
    setDataAtom(data);
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

  // service group 팝업에서 자식 노드를 클릭하면 사이드의 ChartsBoard만 해당 노드로 갱신한다.
  const handleClickSubNode: ServerMapCoreProps['onClickSubNode'] = (subNode) => {
    setServerMapCurrentTarget({
      id: subNode.key,
      applicationName: subNode.applicationName,
      serviceType: subNode.serviceType,
      imgPath: getServerImagePath(subNode),
      type: 'node',
    });
    setCurrentServer(undefined);
  };

  // service group 링크 팝업에서 자식 링크를 클릭하면 사이드의 ChartsBoard만 해당 링크로 갱신한다.
  const handleClickSubLink: ServerMapCoreProps['onClickSubLink'] = (subLink) => {
    setServerMapCurrentTarget({
      id: subLink.key,
      source: subLink.from,
      target: subLink.to,
      type: 'edge',
    });
    setCurrentServer(undefined);
  };

  const handleMergeStateChange = () => {
    if (data) {
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
    }
  };

  return (
    <ServerMapCore
      data={data}
      isLoading={isLoading}
      error={error}
      forceLayoutUpdate
      onClickNode={handleClickNode}
      onClickEdge={handleClickEdge}
      onClickSubNode={handleClickSubNode}
      onClickSubLink={handleClickSubLink}
      selectedSubNodeId={serverMapCurrentTarget?.id}
      selectedSubLinkId={serverMapCurrentTarget?.id}
      onMergeStateChange={handleMergeStateChange}
      baseNodeId={getBaseNodeId({
        application,
        applicationMapData: data?.applicationMapData,
      })}
      inputPlaceHolder={t('COMMON.SEARCH_INPUT')}
      {...props}
    />
  );
};
