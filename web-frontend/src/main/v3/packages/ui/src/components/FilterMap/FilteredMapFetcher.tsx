import { useSetAtom } from 'jotai';
import { currentServerAtom, serverMapCurrentTargetAtom } from '@pinpoint-fe/ui/src/atoms';
import { getBaseNodeId, getServerImagePath, parseBaseNodeId } from '@pinpoint-fe/ui/src/utils';
import { Edge, MergedEdge, MergedNode, Node } from '@pinpoint-fe/server-map';
import { useFilteredMapParameters } from '@pinpoint-fe/ui/src/hooks';
import { useFilteredMapData } from '@pinpoint-fe/ui/src/hooks/serverMap/useFilteredMapData';
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
  const setCurrentServer = useSetAtom(currentServerAtom);
  const setServerMapCurrentTarget = useSetAtom(serverMapCurrentTargetAtom);
  const { application } = useFilteredMapParameters();
  const { serverMapData, data, error } = useFilteredMapData(isPaused);
  const { t } = useTranslation();

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
  };

  return (
    <ServerMapCore
      data={serverMapData}
      error={error}
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
