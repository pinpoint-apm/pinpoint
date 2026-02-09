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
import { getParsedDate, getInspectorPath } from '@pinpoint-fe/ui/src/utils';
import {
  useGetAgentOverview,
  useSearchParameters,
  useServerMapLinkedData,
} from '@pinpoint-fe/ui/src/hooks';
import { ServerList as SL, ServerListProps, Button, ServerListSkeleton } from '@pinpoint-fe/ui';
import { upperCase } from 'lodash';

export interface ServerListFetcherProps extends ServerListProps {
  nodeStatistics?: GetHistogramStatistics.Response;
  disableFetch?: boolean;
}

export const ServerListFetcher = ({ nodeStatistics, disableFetch }: ServerListFetcherProps) => {
  const { searchParameters } = useSearchParameters();
  const currentTarget = useAtomValue(serverMapCurrentTargetAtom);
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

  const { data, isLoading } = useGetAgentOverview({
    application: currentTargetData?.applicationName,
    serviceTypeName: currentTargetData?.serviceType,
    serviceTypeCode: currentTargetData?.serviceTypeCode,
    applicationPairs: JSON.stringify(applicationPairs),
    from: getParsedDate(searchParameters.from).getTime(),
    to: getParsedDate(searchParameters.to).getTime(),
  });

  React.useEffect(() => {
    if (data) {
      setCurrentServer(data?.[0]);
    }
  }, [data, currentTarget]);

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
    <Button variant={'outline'} className="h-5 px-1 text-xs border border-primary" asChild>
      {children}
    </Button>
  );
};
