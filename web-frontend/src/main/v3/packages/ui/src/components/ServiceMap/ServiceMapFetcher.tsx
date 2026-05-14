import React from 'react';
import { useAtomValue, useSetAtom } from 'jotai';
import { MergedEdge, MergedNode } from '@pinpoint-fe/server-map';
import {
  serverMapDataAtom,
  currentServerAtom,
  serverMapCurrentTargetAtom,
} from '@pinpoint-fe/ui/src/atoms';
import {
  GetServerMap,
  GetServiceMap,
  EXPERIMENTAL_CONFIG_KEYS,
} from '@pinpoint-fe/ui/src/constants';
import {
  useExperimentals,
  useGetServiceMap,
  useServerMapSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { useTranslation } from 'react-i18next';
import {
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

// /serviceMap 응답을 GetServerMap.Response 형태로 변환.
// type:'service' 그룹은 그래프상 단일 노드로 그리되, 원본 자식 노드는 subNodes 필드에 보관해
// 그래프에서 노드를 좌클릭했을 때 팝업으로 자식 리스트를 펼칠 수 있게 한다.
const flattenServiceMapResponse = (
  data: GetServiceMap.Response | undefined,
): GetServerMap.Response | undefined => {
  if (!data) return undefined;

  const emptyResponseStatistics: GetServerMap.ResponseStatistics = {
    Tot: 0,
    Sum: 0,
    Avg: 0,
    Max: 0,
  };
  const emptyHistogram: GetServerMap.Histogram = {
    '1s': 0,
    '3s': 0,
    '5s': 0,
    Slow: 0,
    Error: 0,
  };

  const nodeDataArray: GetServerMap.NodeData[] = [];
  for (const entry of data.applicationMapData.nodeDataArray) {
    if (entry.type === 'service') {
      const innerNodes = entry.nodes;
      const firstNode = innerNodes[0];
      nodeDataArray.push({
        key: entry.key,
        applicationName: entry.serviceName,
        serviceType: firstNode?.serviceType ?? 'UNKNOWN',
        serviceTypeCode: firstNode?.serviceTypeCode ?? 0,
        nodeCategory: firstNode?.nodeCategory ?? GetServerMap.NodeCategory.SERVER,
        isAuthorized: true,
        totalCount: innerNodes.reduce((acc, n) => acc + (n.totalCount ?? 0), 0),
        errorCount: innerNodes.reduce((acc, n) => acc + (n.errorCount ?? 0), 0),
        slowCount: innerNodes.reduce((acc, n) => acc + (n.slowCount ?? 0), 0),
        hasAlert: innerNodes.some((n) => n.hasAlert),
        responseStatistics: emptyResponseStatistics,
        histogram: emptyHistogram,
        apdex: {
          apdexScore: 0,
          apdexFormula: { satisfiedCount: 0, toleratingCount: 0, totalSamples: 0 },
        },
        timeSeriesHistogram: [],
        instanceCount: innerNodes.length,
        instanceErrorCount: 0,
        agents: [],
        subNodes: innerNodes,
      });
    } else {
      nodeDataArray.push(entry);
    }
  }

  const linkDataArray: GetServerMap.LinkData[] = [];
  for (const entry of data.applicationMapData.linkDataArray) {
    if (entry.type === 'service') {
      const innerLinks = entry.links;
      const firstLink = innerLinks[0];
      linkDataArray.push({
        key: entry.key,
        from: entry.from,
        to: entry.to,
        sourceInfo: firstLink?.sourceInfo,
        targetInfo: firstLink?.targetInfo,
        filter: firstLink?.filter,
        responseStatistics: emptyResponseStatistics,
        histogram: emptyHistogram,
        timeSeriesHistogram: [],
        totalCount: innerLinks.reduce((acc, l) => acc + (l.totalCount ?? 0), 0),
        errorCount: innerLinks.reduce((acc, l) => acc + (l.errorCount ?? 0), 0),
        slowCount: innerLinks.reduce((acc, l) => acc + (l.slowCount ?? 0), 0),
        hasAlert: innerLinks.some((l) => l.hasAlert),
        subLinks: innerLinks,
      } as GetServerMap.LinkData);
    } else {
      linkDataArray.push(entry);
    }
  }

  return {
    applicationMapData: {
      ...data.applicationMapData,
      nodeDataArray,
      linkDataArray,
    },
  };
};

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
