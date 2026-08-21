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
import {
  useServerMapSearchParameters,
  useServerMapTargetServiceName,
  useTabFocus,
} from '@pinpoint-fe/ui/src/hooks';
import {
  CurrentTarget,
  serverMapCurrentTargetAtom,
  serverMapCurrentTargetDataAtom,
  serverMapDataAtom,
  serverMapChartTypeAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { APP_SETTING_KEYS, ApplicationType, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { getServerImagePath } from '@pinpoint-fe/ui/src/utils';
import { cn } from '@pinpoint-fe/ui/src/lib';

export interface RealtimeProps {
  /**
   * map 영역에 그릴 컴포넌트(기본값 ServerMap).
   * servicemap 실시간 보기는 ServiceMap을 넘긴다. 화면은 그대로 두고 map을 그리는 API만 갈린다.
   */
  MapView?: typeof ServerMap;
  /**
   * map이 기준 application을 필요로 하는지 여부(기본값 true).
   *
   * false면 경로에 application이 없어도 map을 그린다. servicemap에서 DEFAULT가 아닌 service를
   * 볼 때가 여기에 해당하고, 그때는 map에서 노드를 클릭해야 우측 패널의 조회 대상이 정해진다.
   */
  requiresApplication?: boolean;
}

export const Realtime = ({ MapView = ServerMap, requiresApplication = true }: RealtimeProps) => {
  const chartType = useAtomValue(serverMapChartTypeAtom);
  const isFocus = useTabFocus();
  const containerRef = React.useRef<HTMLDivElement>(null);
  const { application } = useServerMapSearchParameters();
  const [serverMapCurrentTarget, setServerMapCurrentTarget] = useAtom(serverMapCurrentTargetAtom);
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom) as GetServerMap.NodeData;
  const serverMapData = useAtomValue(serverMapDataAtom);
  const { t } = useTranslation();
  // 기준 application이 필요 없는 map은 고를 대상이 없으므로 곧바로 그린다.
  const showMap = !requiresApplication || !!application;

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
      serverMapData &&
      !(
        currentTargetData &&
        (currentTargetData as GetServerMap.NodeData)?.nodeCategory ===
          GetServerMap.NodeCategory.SERVER
      )
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
  // servicemap 실시간 보기에서는 다른 service의 노드도 고를 수 있다. 우측 패널의 조회는
  // 화면의 service가 아니라 고른 노드의 service로 나가야 한다. 차트들에 prop으로 내려준다.
  const targetServiceName = useServerMapTargetServiceName();

  return (
    <div ref={containerRef} className="relative flex flex-1 h-full overflow-x-hidden">
      <div
        className="relative h-full"
        style={{ width: `calc(100% - ${currentPanelWidth + resizeHandleWidth}px)` }}
      >
        {showMap && (
          <ResizablePanelGroup
            direction="vertical"
            autoSaveId={APP_SETTING_KEYS.REALTIME_ACTIVE_REQUEST_RESIZABLE}
          >
            <ResizablePanel minSize={10} maxSize={90}>
              {isFocus && <MapView shouldPoll={true} />}
            </ResizablePanel>
            <ResizableHandle withHandle />
            <ResizablePanel minSize={10} maxSize={90} className="!overflow-auto">
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
          disabledBreak={true}
          timestamp={
            serverMapData?.applicationMapData
              ?.timestamp as GetServerMap.ApplicationMapData['timestamp']
          }
          nodeData={currentTargetData?.isAuthorized === false ? undefined : currentTargetData}
          emptyMessage={t('COMMON.NO_DATA')}
          header={
            <ChartsBoardHeader
              // 기준 application이 없는 map에서는 노드를 고르기 전까지 보여줄 대상이 없다.
              // 빈 객체를 넘기면 이름 없는 아이콘만 덩그러니 남는다.
              currentTarget={
                serverMapCurrentTarget ||
                (application
                  ? {
                      ...application,
                      type: 'node',
                    }
                  : null)
              }
            />
          }
        >
          {!serverMapCurrentTarget ? (
            // 고른 대상이 없으면 헤더도 차트도 그릴 것이 없어 패널이 통째로 비어 보인다.
            // 무엇을 해야 하는지 알려준다. (기준 application이 없는 map에서만 생기는 상태)
            <div className="flex justify-center items-center w-full h-full text-muted-foreground">
              {t('SERVER_MAP.SELECT_NODE_FOR_CHART')}
            </div>
          ) : serverMapCurrentTarget?.nodes || serverMapCurrentTarget?.edges ? (
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
                  <div
                    className={cn('w-full p-5', {
                      'aspect-[1.618] mb-12 ': chartType === 'scatter',
                      'aspect-[1.4]': chartType === 'heatmap',
                    })}
                  >
                    <div className="h-7">
                      <ApdexScore
                        shouldPoll={true}
                        nodeData={currentTargetData || application}
                        serviceName={targetServiceName}
                      ></ApdexScore>
                    </div>
                    {chartType === 'scatter' ? (
                      <ScatterChart
                        node={serverMapCurrentTarget || (application as ApplicationType)}
                        realtime={true}
                        serviceName={targetServiceName}
                      />
                    ) : (
                      // <div className="w-full pl-3 pt-5 pr-10 pb-8 aspect-[1.3]">
                      <Heatmap
                        nodeData={currentTargetData || (application as ApplicationType)}
                        realtime={true}
                        serviceName={targetServiceName}
                      />
                      // </div>
                    )}
                  </div>
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
