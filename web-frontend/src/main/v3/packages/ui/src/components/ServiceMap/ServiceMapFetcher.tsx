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
  useIsDefaultService,
  useServerMapSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { useTranslation } from 'react-i18next';
import {
  findServiceGroupLink,
  findServiceGroupNode,
  flattenServiceMapResponse,
  getBaseNodeId,
  getServerImagePath,
  parseBaseNodeId,
  toBasicISOString,
} from '@pinpoint-fe/ui/src/utils';
import { ServerMapCore, ServerMapCoreProps } from '../ServerMap/ServerMapCore';

export interface ServiceMapFetcherProps extends Pick<
  ServerMapCoreProps,
  'onClickMenuItem' | 'onApplyChangedOption' | 'queryOption'
> {
  shouldPoll?: boolean;
}

export const ServiceMapFetcher = ({ shouldPoll, ...props }: ServiceMapFetcherProps) => {
  const setDataAtom = useSetAtom(serverMapDataAtom);
  const setCurrentServer = useSetAtom(currentServerAtom);
  const setServerMapCurrentTarget = useSetAtom(serverMapCurrentTargetAtom);
  const serverMapCurrentTarget = useAtomValue(serverMapCurrentTargetAtom);
  const { application, dateRange } = useServerMapSearchParameters();
  const isDefaultService = useIsDefaultService();
  const experimentalOption = useExperimentals();
  const useStatisticsAgentState =
    experimentalOption[EXPERIMENTAL_CONFIG_KEYS.USE_STATISTICS_AGENT_STATE].value || true;

  const {
    data: rawData,
    isLoading,
    error,
  } = useGetServiceMap(
    {
      // DEFAULT가 아닌 service는 application 파라미터를 아예 싣지 않는다. 백엔드가 그 service의
      // 전체 application을 source로 쓰므로 값이 무시되는데, 실어 보내면 쿼리 키만 갈라져
      // 같은 map을 여러 벌 캐싱하게 된다.
      applicationName: isDefaultService ? (application?.applicationName ?? '') : undefined,
      serviceTypeName: isDefaultService ? application?.serviceType : undefined,
      from: toBasicISOString(dateRange.from),
      to: toBasicISOString(dateRange.to),
      useStatisticsAgentState,
    },
    { requiresApplication: isDefaultService, shouldPoll: !!shouldPoll },
  );

  const data = React.useMemo(() => flattenServiceMapResponse(rawData), [rawData]);
  // service 전체를 모아 그린 map에는 기준이 되는 application이 없다. 빈 문자열을 넘기면 서버맵이
  // 특정 노드를 중심에 두지 않고 전체 노드를 기준으로 센터링한다.
  const baseNodeId = getBaseNodeId({
    application: isDefaultService ? application : null,
    applicationMapData: data?.applicationMapData,
  });
  const { t } = useTranslation();

  React.useEffect(() => {
    setDataAtom(data);
  }, [data]);

  const handleClickNode: ServerMapCoreProps['onClickNode'] = ({ data: clickedData, eventType }) => {
    const { label, type, imgPath, id, nodes } = clickedData as MergedNode;
    if (eventType === 'left' || eventType === 'programmatic') {
      // service group(접힌 service) 노드는 그 자체로 조회 대상이 될 수 없다. 좌클릭은 자식
      // application 목록 팝업을 열 뿐이고, 목록에서 b-1 같은 application을 골라야 비로소
      // 선택이다(→ handleClickSubNode). group을 선택으로 만들면 우측 패널이 열리면서
      // 고르지도 않은 service가 선택된 것처럼 보인다(패널에 그릴 것은 없어 비어 있다).
      if (findServiceGroupNode(data?.applicationMapData?.nodeDataArray, id)) {
        return;
      }

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

  const handleClickEdge: ServerMapCoreProps['onClickEdge'] = ({ data: clickedData, eventType }) => {
    const { id, source, target, edges } = clickedData as MergedEdge;
    if (eventType === 'left') {
      // 한쪽 끝이 service group인 링크도 마찬가지다. 팝업에서 자식 링크를 골라야 선택이다.
      // (→ handleClickSubLink)
      if (findServiceGroupLink(data?.applicationMapData?.linkDataArray, id)) {
        return;
      }

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
    // service 전체를 모아 그린 map에는 기준 application이 없으므로,
    // 병합 상태가 바뀌어도 특정 노드를 선택된 상태로 만들지 않는다.
    if (!isDefaultService) {
      return;
    }

    if (data) {
      const { applicationName, serviceType } = parseBaseNodeId(baseNodeId);

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
      // 실시간에서는 2초마다 데이터가 갱신된다. 매번 레이아웃을 다시 잡으면 노드가 계속 튄다.
      forceLayoutUpdate={!shouldPoll}
      onClickNode={handleClickNode}
      onClickEdge={handleClickEdge}
      onClickSubNode={handleClickSubNode}
      onClickSubLink={handleClickSubLink}
      selectedSubNodeId={serverMapCurrentTarget?.id}
      selectedSubLinkId={serverMapCurrentTarget?.id}
      onMergeStateChange={handleMergeStateChange}
      baseNodeId={baseNodeId}
      inputPlaceHolder={t('COMMON.SEARCH_INPUT')}
      {...props}
    />
  );
};
