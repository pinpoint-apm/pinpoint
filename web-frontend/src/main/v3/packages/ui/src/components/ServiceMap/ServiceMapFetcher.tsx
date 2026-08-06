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

export const ServiceMapFetcher = ({
  shouldPoll: _shouldPoll,
  ...props
}: ServiceMapFetcherProps) => {
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
    { requiresApplication: isDefaultService },
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
      forceLayoutUpdate
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
