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
  useGetServerMapDataV2,
  useServerMapSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { useTranslation } from 'react-i18next';
import { getBaseNodeId, getServerImagePath } from '@pinpoint-fe/ui/src/utils';
import { ServerMapCore, ServerMapCoreProps } from './ServerMapCore';

export interface ServerMapFetcherProps extends Pick<
  ServerMapCoreProps,
  'onClickMenuItem' | 'onApplyChangedOption' | 'queryOption'
> {
  shouldPoll?: boolean;
}

export const ServerMapFetcher = ({ shouldPoll, ...props }: ServerMapFetcherProps) => {
  const setDataAtom = useSetAtom(serverMapDataAtom);
  const setCurrentServer = useSetAtom(currentServerAtom);
  const setServerMapCurrentTarget = useSetAtom(serverMapCurrentTargetAtom);
  const { application } = useServerMapSearchParameters();
  const experimentalOption = useExperimentals();
  const useStatisticsAgentState = experimentalOption.statisticsAgentState.value || true;

  const { data, isLoading, error } = useGetServerMapDataV2({
    shouldPoll: !!shouldPoll,
    useStatisticsAgentState,
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

  const handleMergeStateChange = () => {
    if (data) {
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
    }
  };

  return (
    <ServerMapCore
      data={data || {}}
      isLoading={isLoading}
      error={error}
      forceLayoutUpdate={!shouldPoll}
      onClickNode={handleClickNode}
      onClickEdge={handleClickEdge}
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
