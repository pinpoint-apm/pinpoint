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
  AgentActiveThreadFetcher,
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
  ErrorBoundary,
  Heatmap,
  ChartTypeButtons,
} from '..';
import { useServerMapSearchParameters, useTabFocus } from '@pinpoint-fe/ui/src/hooks';
import {
  CurrentTarget,
  serverMapCurrentTargetAtom,
  serverMapCurrentTargetDataAtom,
  serverMapDataAtom,
  serverMapChartTypeAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { APP_SETTING_KEYS, ApplicationType, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { getServerImagePath } from '@pinpoint-fe/ui/src/utils';

export interface RealtimeProps {}

export const Realtime = () => {
  const chartType = useAtomValue(serverMapChartTypeAtom);
  const isFocus = useTabFocus();
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
    if (serverMapData && !currentTargetData) {
      return true;
    }
    return (
      serverMapData && !(currentTargetData && (currentTargetData as GetServerMap.NodeData)?.isWas)
    );
  }, [serverMapData, currentTargetData]);

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
        {application && (
          <ResizablePanelGroup
            direction="vertical"
            autoSaveId={APP_SETTING_KEYS.REALTIME_ACTIVE_REQUEST_RESIZABLE}
          >
            <ResizablePanel>{isFocus && <ServerMap shouldPoll={true} />}</ResizablePanel>
            <ResizableHandle withHandle />
            <ResizablePanel className="!overflow-auto">
              {isFocus && (
                <ErrorBoundary>
                  <AgentActiveThreadFetcher />
                </ErrorBoundary>
              )}
            </ResizablePanel>
          </ResizablePanelGroup>
        )}
      </div>
      <div
        className="min-w-[500px]"
        style={{
          width: currentPanelWidth + resizeHandleWidth,
          borderLeftWidth: resizeHandleWidth,
        }}
      >
        <ChartsBoard
          timestamp={
            serverMapData?.applicationMapData
              ?.timestamp as GetServerMap.ApplicationMapData['timestamp']
          }
          nodeData={currentTargetData?.isAuthorized === false ? undefined : currentTargetData}
          emptyMessage={t('COMMON.NO_DATA')}
          header={
            <ChartsBoardHeader
              currentTarget={
                serverMapCurrentTarget || {
                  ...application,
                  type: 'node',
                }
              }
            />
          }
        >
          {serverMapCurrentTarget?.nodes || serverMapCurrentTarget?.edges ? (
            <MergedServerSearchList
              list={getClickedMergedNodeList(serverMapCurrentTarget)}
              onClickItem={handleClickMergedItem}
            />
          ) : (
            <>
              {(currentTargetData as GetServerMap.NodeData)?.instanceCount ? (
                <div className="flex items-center h-12 py-2.5 px-4">
                  <ChartTypeButtons />
                  <InstanceCount className="ml-auto" nodeData={currentTargetData} />
                </div>
              ) : null}
              {!shouldHideScatter() && isFocus && (
                <>
                  {chartType === 'scatter' ? (
                    <div className="w-full p-5 mb-12 aspect-[1.618]">
                      <div className="h-7">
                        <ApdexScore
                          shouldPoll={true}
                          nodeData={currentTargetData || application}
                        ></ApdexScore>
                      </div>
                      <ScatterChart
                        node={serverMapCurrentTarget || (application as ApplicationType)}
                        realtime={true}
                      />
                    </div>
                  ) : (
                    <div className="w-full pl-3 pt-5 pr-10 pb-8 aspect-[1.3]">
                      <Heatmap
                        nodeData={currentTargetData || (application as ApplicationType)}
                        realtime={true}
                      />
                    </div>
                  )}
                  <Separator />
                </>
              )}
            </>
          )}
        </ChartsBoard>
      </div>
    </div>
  );
};
