import React from 'react';
import { useAtomValue, useSetAtom } from 'jotai';
import {
  serverMapCurrentTargetDataAtom,
  currentServerAtom,
  currentServerAgentIdAtom,
  serverMapDataAtom,
  serverMapCurrentTargetAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { GetServerMap, BASE_PATH, GetHistogramStatistics } from '@pinpoint-fe/ui/src/constants';
import { getParsedDate, getInspectorPath, toBasicISOString } from '@pinpoint-fe/ui/src/utils';
import {
  useGetAgentOverview,
  useSearchParameters,
  useServerMapLinkedData,
} from '@pinpoint-fe/ui/src/hooks';
import { ServerList as SL, ServerListProps, Button, ServerListSkeleton } from '@pinpoint-fe/ui';
import { upperCase } from 'lodash';

export interface ServerListFetcherProps extends ServerListProps {
  nodeStatistics?: GetHistogramStatistics.Response;
  /**
   * 조회 대상이 소속된 service. 화면의 service와 다를 때만 넘긴다(map에서 다른 service의 노드를
   * 고른 경우). 넘기지 않으면 화면의 service로 조회한다.
   */
  serviceName?: string;
}

export const ServerListFetcher = ({ nodeStatistics, serviceName }: ServerListFetcherProps) => {
  const { searchParameters } = useSearchParameters();
  // 값은 쓰지 않지만 atom 구독은 유지해야 하므로 호출은 남긴다.
  useAtomValue(serverMapCurrentTargetAtom);
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom) as GetServerMap.NodeData;
  const setCurrentServer = useSetAtom(currentServerAtom);
  const currentServerAgent = useAtomValue(currentServerAgentIdAtom);
  const serverMapData = useAtomValue(serverMapDataAtom);
  const serverMapLinkedData = useServerMapLinkedData({
    serverMapData: serverMapData?.applicationMapData as GetServerMap.ApplicationMapData,
    currentTargetData,
  });
  const applicationPairs = {
    from: serverMapLinkedData?.from.map(({ applicationName, serviceTypeCode }) => [
      applicationName,
      serviceTypeCode,
    ]),
    to: serverMapLinkedData?.to.map(({ applicationName, serviceTypeCode }) => [
      applicationName,
      serviceTypeCode,
    ]),
  };

  const { data, isLoading } = useGetAgentOverview(
    {
      application: currentTargetData?.applicationName,
      serviceTypeName: currentTargetData?.serviceType,
      serviceTypeCode: currentTargetData?.serviceTypeCode,
      applicationPairs: JSON.stringify(applicationPairs),
      from: toBasicISOString(getParsedDate(searchParameters.from)),
      to: toBasicISOString(getParsedDate(searchParameters.to)),
    },
    serviceName,
  );

  React.useEffect(() => {
    if (data) {
      setCurrentServer(data?.[0]);
    }
  }, [currentTargetData?.key]); // currentTarget이 바뀌어서 agent 목록이 바뀐 경우에만 동작

  const handleClickItem: ServerListProps['onClick'] = (instance) => {
    setCurrentServer(instance);
  };

  const renderItem: ServerListProps['itemRenderer'] = (instance) => {
    return (
      <>
        <div className="flex-1 truncate">{instance?.agentName || instance.agentId}</div>{' '}
        {instance?.linkList?.map((link, index) => {
          return (
            <LinkButton key={index}>
              <a href={link.linkURL} target={'_blank'}>
                {upperCase(link.linkName)}
              </a>
            </LinkButton>
          );
        })}
      </>
    );
  };

  if (isLoading) {
    return (
      <div className="flex h-full">
        <ServerListSkeleton className="h-full border-t border-r" />
      </div>
    );
  }

  return (
    <SL
      data={data || []}
      className={'border-t border-r bg-neutral-100'}
      statistics={nodeStatistics}
      selectedId={currentServerAgent}
      onClick={handleClickItem}
      itemRenderer={renderItem}
      onClickInspectorLink={(agentId) => {
        window.open(
          `${BASE_PATH}${getInspectorPath(currentTargetData, searchParameters)}&agentId=${agentId}`,
        );
      }}
    ></SL>
  );
};

const LinkButton = ({ children }: { children: React.ReactNode }) => {
  return (
    <Button variant={'outline'} className="px-1 h-5 text-xs border border-primary" asChild>
      {children}
    </Button>
  );
};
