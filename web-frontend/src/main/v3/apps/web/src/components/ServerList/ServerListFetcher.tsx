import React from 'react';
import useSWR from 'swr';
import { useAtomValue, useSetAtom } from 'jotai';
import {
  serverMapCurrentTargetDataAtom,
  currentServerAtom,
  currentNodeStatisticsAtom,
  currentServerAgentIdAtom,
} from '@pinpoint-fe/atoms';
import { END_POINTS, GetServerMap, SearchApplication, BASE_PATH } from '@pinpoint-fe/constants';
import { convertParamsToQueryString, getParsedDate, getV2InspectorUrl } from '@pinpoint-fe/utils';
import { useSearchParameters, swrConfigs } from '@pinpoint-fe/hooks';
import { getInspectorPath } from '@pinpoint-fe/utils';
import { ServerList as SL, ServerListProps } from '@pinpoint-fe/ui';

export interface ServerListFetcherProps extends ServerListProps {
  disableFetch?: boolean;
}

export const ServerListFetcher = ({ disableFetch }: ServerListFetcherProps) => {
  const { search, searchParameters } = useSearchParameters();
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom) as GetServerMap.NodeData;
  const currentNodeStatistics = useAtomValue(currentNodeStatisticsAtom);
  const setCurrentServer = useSetAtom(currentServerAtom);
  const currentServerAgent = useAtomValue(currentServerAgentIdAtom);

  const [queryParams, setQueryParams] = React.useState<SearchApplication.Parameters>({
    application: currentTargetData?.applicationName,
    sortBy: 'AGENT_ID_ASC',
    from: getParsedDate(searchParameters.from).getTime(),
    to: getParsedDate(searchParameters.to).getTime(),
  });
  const getQueryString = React.useCallback(() => {
    if (queryParams.from && queryParams.to && queryParams.application && queryParams.sortBy) {
      return '?' + convertParamsToQueryString(queryParams);
    }

    return '';
  }, [queryParams]);
  const { data } = useSWR<SearchApplication.Response>(
    getQueryString() && !disableFetch
      ? [`${END_POINTS.SEARCH_APPLICATION}${getQueryString()}`]
      : null,
    swrConfigs,
  );

  React.useEffect(() => {
    setQueryParams((prev: SearchApplication.Parameters) => ({
      ...prev,
      from: getParsedDate(searchParameters.from).getTime(),
      to: getParsedDate(searchParameters.to).getTime(),
    }));
  }, [search]);

  React.useEffect(() => {
    setQueryParams((prev: SearchApplication.Parameters) => ({
      ...prev,
      application: currentTargetData?.applicationName,
    }));
  }, [currentTargetData]);

  React.useEffect(() => {
    if (data) {
      const servers = getServers();
      setCurrentServer(servers?.[0]);
    }
  }, [data]);

  const getServers = () => {
    return data?.reduce<SearchApplication.Instance[]>((prev, curr) => {
      curr.instancesList.forEach((instance) => {
        prev.push(instance);
      });
      return prev;
    }, []);
  };

  const handleClickItem: ServerListProps['onClick'] = (instance) => {
    setCurrentServer(instance);
  };

  const renderGroupName: ServerListProps['groupNameRenderer'] = (application) => {
    return <div className="flex-1 truncate">{application.groupName}</div>;
  };

  const renderItem: ServerListProps['itemRenderer'] = (application, instance) => {
    return <div className="flex-1 truncate">{instance?.agentName || instance.agentId}</div>;
  };

  return (
    <SL
      data={data}
      className={'border-t border-r bg-neutral-100'}
      statistics={currentNodeStatistics}
      selectedId={currentServerAgent}
      onClick={handleClickItem}
      groupNameRenderer={renderGroupName}
      itemRenderer={renderItem}
      onClickInspectorLink={(agentId) => {
        window.open(`${getV2InspectorUrl(currentTargetData, searchParameters)}/${agentId}`);
        // window.open(
        //   `${BASE_PATH}${getInspectorPath(currentTargetData, searchParameters)}&agentId=${agentId}`,
        // );
      }}
    ></SL>
  );
};
