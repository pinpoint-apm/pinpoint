import React from 'react';
import { useAtom, useAtomValue } from 'jotai';
import { useTranslation } from 'react-i18next';
import {
  ChartsBoard,
  ChartsBoardHeader,
  ServerMap,
  ApdexScore,
  Separator,
  InstanceCount,
  MergedServerSearchListProps,
  MergedServerSearchList,
  ScatterChart,
  useLayoutWithHorizontalResizable,
} from '..';
import { useServerMapSearchParameters } from '@pinpoint-fe/hooks';
import {
  CurrentTarget,
  serverMapCurrentTargetAtom,
  serverMapCurrentTargetDataAtom,
  serverMapDataAtom,
} from '@pinpoint-fe/atoms';
import { GetServerMap } from '@pinpoint-fe/constants';
import { getServerImagePath } from '@pinpoint-fe/utils';

export interface RealtimeProps {}

export const Realtime = () => {
  const containerRef = React.useRef<HTMLDivElement>(null);
  const { application } = useServerMapSearchParameters();
  const [serverMapCurrentTarget, setServerMapCurrentTarget] = useAtom(serverMapCurrentTargetAtom);
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom) as GetServerMap.NodeData;
  const serverMapData = useAtomValue(serverMapDataAtom);
  const { t } = useTranslation();

  React.useEffect(() => {
    if (application) {
      setServerMapCurrentTarget({
        ...application,
        imgPath: getServerImagePath(application),
        type: 'node',
      });
    } else {
      setServerMapCurrentTarget(undefined);
    }
  }, [application?.applicationName, application?.serviceType]);

  const shouldHideScatter = React.useCallback(() => {
    if (!currentTargetData) {
      return true;
    }
    return !(currentTargetData && (currentTargetData as GetServerMap.NodeData)?.isWas);
  }, [currentTargetData]);

  const handleClickMergedItem: MergedServerSearchListProps['onClickItem'] = (nodeData) => {
    const { key, applicationName, serviceType } = nodeData;
    setServerMapCurrentTarget({
      id: key,
      applicationName,
      serviceType,
      imgPath: getServerImagePath(nodeData),
      type: 'node',
      nodes: serverMapCurrentTarget?.nodes,
      edges: serverMapCurrentTarget?.edges,
    });
  };

  const getClickedMergedNodeList = ({ nodes, edges }: CurrentTarget) => {
    const nodeIds = nodes
      ? nodes.map((node) => node.id)
      : edges
      ? edges.map((edge) => edge.target)
      : [];

    return serverMapData?.applicationMapData.nodeDataArray
      ?.filter(({ key }: GetServerMap.NodeData) => nodeIds.includes(key))
      .sort((node1, node2) => node2.totalCount - node1.totalCount);
  };

  const { currentPanelWidth, resizeHandleWidth } = useLayoutWithHorizontalResizable();

  return (
    <div ref={containerRef} className="relative flex flex-1 h-full overflow-x-hidden">
      <div
        className="relative h-full"
        style={{ width: `calc(100% - ${currentPanelWidth + resizeHandleWidth}px)` }}
      >
        {application && <ServerMap shouldPoll={true} />}
      </div>
      <div
        className="min-w-[500px]"
        style={{
          width: currentPanelWidth + resizeHandleWidth,
          borderLeftWidth: resizeHandleWidth,
        }}
      >
        {serverMapCurrentTarget && currentTargetData && (
          <ChartsBoard
            nodeData={currentTargetData?.isAuthorized === false ? undefined : currentTargetData}
            emptyMessage={t('COMMON.NO_DATA')}
            header={<ChartsBoardHeader currentTarget={serverMapCurrentTarget} />}
          >
            {serverMapCurrentTarget.nodes || serverMapCurrentTarget.edges ? (
              <MergedServerSearchList
                list={getClickedMergedNodeList(serverMapCurrentTarget)}
                onClickItem={handleClickMergedItem}
              />
            ) : (
              <>
                <div className="flex items-center h-12 py-2.5 px-4">
                  <InstanceCount className="ml-auto" nodeData={currentTargetData} />
                </div>
                {!shouldHideScatter() && (
                  <>
                    <div className="w-full p-5 mb-12 aspect-[1.618]">
                      <div className="h-7">
                        <ApdexScore shouldPoll={true} nodeData={currentTargetData}></ApdexScore>
                      </div>
                      <ScatterChart node={serverMapCurrentTarget} realtime={true} />
                    </div>
                    <Separator />
                  </>
                )}
              </>
            )}
          </ChartsBoard>
        )}
      </div>
    </div>
  );
};
