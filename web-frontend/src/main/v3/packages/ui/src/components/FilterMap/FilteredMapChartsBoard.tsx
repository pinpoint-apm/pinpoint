import React from 'react';
import { ChartsBoard, ChartsBoardProps } from '../ChartsBoard';
import {
  BASE_PATH,
  Configuration,
  FilteredMapType as FilteredMap,
  GetHistogramStatistics,
  GetServerMap,
  SCATTER_DATA_TOTAL_KEY,
} from '@pinpoint-fe/ui/src/constants';
import {
  ApdexScore,
  ScatterChartStatic,
  Separator,
  ChartsBoardHeader,
  Button,
  Drawer,
  InstanceCount,
  MergedServerSearchList,
  MergedServerSearchListProps,
} from '..';
import {
  serverMapDataAtom,
  serverMapCurrentTargetAtom,
  serverMapCurrentTargetDataAtom,
  currentServerAtom,
  scatterDataByApplicationKeyAtom,
  CurrentTarget,
} from '@pinpoint-fe/ui/src/atoms';
import { useTranslation } from 'react-i18next';
import { useAtomValue, useAtom } from 'jotai';
import {
  getApplicationKey,
  getServerImagePath,
  getTransactionListPath,
  getTransactionListQueryString,
} from '@pinpoint-fe/ui/src/utils';
import { useFilteredMapParameters } from '@pinpoint-fe/ui/src/hooks';
import { ServerListForCommon } from '@pinpoint-fe/ui/src/components/ServerList/ServerListForCommon';
import { MdArrowForwardIos, MdArrowBackIosNew } from 'react-icons/md';
import { PiArrowSquareOut } from 'react-icons/pi';

export interface FilteredMapChartsBoardProps extends Omit<
  ChartsBoardProps,
  'timestamp' | 'nodeData' | 'header'
> {
  authorizationGuideUrl?: string;
  currentPanelWidth: number;
  SERVER_LIST_WIDTH: number;
  resizeHandleWidth: number;
  FILTERED_MAP_CONTAINER_ID: string;
  configuration?: Configuration;
}

export const FilteredMapChartsBoard = ({
  authorizationGuideUrl,
  currentPanelWidth,
  SERVER_LIST_WIDTH,
  resizeHandleWidth,
  FILTERED_MAP_CONTAINER_ID,
  configuration,
  children,
  ...props
}: FilteredMapChartsBoardProps) => {
  const { t } = useTranslation();
  const { dateRange, application, searchParameters } = useFilteredMapParameters();

  const [openServerView, setOpenServerView] = React.useState(false);

  // /api/servermap/filterServerMap 로 가져온 data
  const serverMapData = useAtomValue(serverMapDataAtom);
  // serverMap에서 클릭 된 target (node 또는 link)
  const [serverMapCurrentTarget, setServerMapCurrentTarget] = useAtom(serverMapCurrentTargetAtom);
  // 클릭 된 serverMap target의 data (serverMapCurrentTarget, serverMapData를 이용해 나온 값) serverMap이 클릭되었을 때 주는 node/link의 데이터가 충분치 않아서 사용하는 값
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom);
  // VIEW SERVERS로 열었을 때 왼쪽에 클릭된 서버
  const currentServer = useAtomValue(currentServerAtom);

  const scatterDataByApplicationKey = useAtomValue(scatterDataByApplicationKeyAtom);

  const shouldHideScatter = () => {
    if (!currentTargetData) {
      return true;
    }
    return !(
      (
        currentTargetData &&
        (currentTargetData as FilteredMap.NodeData)?.nodeCategory ===
          GetServerMap.NodeCategory.SERVER
      )
      // && !currentTargetData?.isMerged
    );
  };

  const getClickedMergedNodeList = ({ nodes, edges }: CurrentTarget) => {
    if (!serverMapData) {
      return [];
    }

    const nodeIds = nodes
      ? nodes.map((node) => node.id)
      : edges
        ? edges.map((edge) => edge.target)
        : [];

    return (serverMapData.applicationMapData.nodeDataArray as FilteredMap.NodeData[])
      .filter(({ key }: FilteredMap.NodeData) => nodeIds.includes(key))
      .sort((node1, node2) => node2.totalCount - node1.totalCount);
  };

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

  const agentHistogramStatisticsData = React.useMemo(() => {
    if (currentServer?.agentId) {
      return {
        histogram: (currentTargetData as FilteredMap.NodeData)?.agentHistogram?.[
          currentServer?.agentId || ''
        ],
        responseStatistics: (currentTargetData as FilteredMap.NodeData)?.agentResponseStatistics?.[
          currentServer?.agentId || ''
        ],
        timeSeriesHistogram:
          (currentTargetData as FilteredMap.NodeData)?.agentTimeSeriesHistogram?.[
            currentServer?.agentId || ''
          ] || [],
      };
    }

    return;
  }, [currentServer?.agentId, currentTargetData]);

  return (
    <>
      <ChartsBoard
        {...props}
        timestamp={serverMapData?.applicationMapData?.timestamp}
        nodeData={
          (currentTargetData as FilteredMap.NodeData)?.isAuthorized === false
            ? undefined
            : (currentTargetData as FilteredMap.NodeData)
        }
        header={
          <ChartsBoardHeader
            currentTarget={
              openServerView
                ? null
                : serverMapCurrentTarget || {
                    ...application,
                    type: 'node',
                  }
            }
          />
        }
        emptyMessage={t('COMMON.NO_DATA')}
      >
        {serverMapCurrentTarget?.nodes ||
        serverMapCurrentTarget?.edges ||
        currentTargetData === undefined ||
        serverMapCurrentTarget?.type === 'edge' ||
        (currentTargetData as FilteredMap.NodeData)?.isAuthorized ? (
          <>
            {serverMapCurrentTarget?.nodes || serverMapCurrentTarget?.edges ? (
              <MergedServerSearchList
                list={getClickedMergedNodeList(serverMapCurrentTarget)}
                onClickItem={handleClickMergedItem}
              />
            ) : (
              <>
                {serverMapCurrentTarget?.type === 'node' &&
                (currentTargetData as FilteredMap.NodeData)?.instanceCount ? (
                  <div className="flex items-center h-12 py-2.5 px-4">
                    <Button
                      className="px-2 py-1 text-xs"
                      variant="outline"
                      onClick={() => setOpenServerView(!openServerView)}
                    >
                      {openServerView ? <MdArrowForwardIos /> : <MdArrowBackIosNew />}
                      <span className="ml-2">VIEW SERVERS</span>
                    </Button>
                    <InstanceCount nodeData={currentTargetData as FilteredMap.NodeData} />
                  </div>
                ) : null}
                {!shouldHideScatter() && application && (
                  <>
                    <div className="w-full p-5 mb-12 aspect-[1.618]">
                      <div className="h-7">
                        <ApdexScore nodeData={currentTargetData as FilteredMap.NodeData} />
                      </div>
                      <ScatterChartStatic
                        application={serverMapCurrentTarget!}
                        data={
                          scatterDataByApplicationKey?.[getApplicationKey(serverMapCurrentTarget)]
                            ?.acc[SCATTER_DATA_TOTAL_KEY]
                        }
                        range={[dateRange.from.getTime(), dateRange.to.getTime()]}
                        selectedAgentId={SCATTER_DATA_TOTAL_KEY}
                        onDragEnd={(data, checkedLables) => {
                          if (checkedLables.length) {
                            window.__pp_scatter_data__ =
                              scatterDataByApplicationKey?.[
                                getApplicationKey(serverMapCurrentTarget)
                              ]?.acc;
                            window.open(
                              `${BASE_PATH}${getTransactionListPath(
                                serverMapCurrentTarget,
                                searchParameters,
                              )}&${getTransactionListQueryString({
                                ...data,
                                checkedLegends: checkedLables,
                                agentId: '',
                              })}&withFilter=true`,
                            );
                          }
                        }}
                      />
                    </div>
                    <Separator />
                  </>
                )}
              </>
            )}
          </>
        ) : (
          <div className="flex justify-center font-semibold pt-25 text-status-fail">
            <a href={authorizationGuideUrl} target="_blank">
              You don't have authorization.
              {authorizationGuideUrl && <PiArrowSquareOut />}
            </a>
          </div>
        )}
      </ChartsBoard>
      <Drawer
        destroyOnClose
        open={openServerView}
        getContainer={`#${FILTERED_MAP_CONTAINER_ID}`}
        contentWrapperStyle={{
          width: currentPanelWidth + SERVER_LIST_WIDTH,
          right: currentPanelWidth + resizeHandleWidth,
        }}
        // afterOpenChange={(openChange) => setServerViewTransitionEnd(openChange)}
        onClose={() => setOpenServerView(false)}
      >
        <div style={{ width: SERVER_LIST_WIDTH }}>
          <div className="flex gap-1 items-center h-12 font-semibold border-b-1 shrink-0">
            <img src={serverMapCurrentTarget?.imgPath} width={52} />
            <div className="truncate">{serverMapCurrentTarget?.applicationName}</div>
          </div>
          <ServerListForCommon
            nodeStatistics={serverMapData as unknown as GetHistogramStatistics.Response}
          />
        </div>
        <div style={{ width: currentPanelWidth }}>
          <ChartsBoard
            header={
              <div className="flex gap-1 items-center h-12 font-semibold border-b-1 shrink-0">
                <div className="flex justify-center items-center">
                  <MdArrowForwardIos />
                </div>
                {currentServer?.agentId}
              </div>
            }
            timestamp={serverMapData?.applicationMapData?.timestamp}
            nodeData={agentHistogramStatisticsData as FilteredMap.NodeData}
          >
            {!shouldHideScatter() && application && (
              <>
                <div className="w-full p-5 mb-12 aspect-[1.618]">
                  <div className="h-7">
                    {currentServer?.agentId && (
                      <ApdexScore
                        nodeData={currentTargetData as GetServerMap.NodeData}
                        agentId={currentServer?.agentId}
                      />
                    )}
                  </div>
                  <ScatterChartStatic
                    application={application}
                    data={
                      currentServer?.agentId
                        ? scatterDataByApplicationKey?.[getApplicationKey(serverMapCurrentTarget)]
                            ?.acc[currentServer?.agentId]
                        : undefined
                    }
                    range={[dateRange.from.getTime(), dateRange.to.getTime()]}
                    selectedAgentId={currentServer?.agentId}
                    onDragEnd={(data, checkedLables) => {
                      if (checkedLables.length) {
                        window.__pp_scatter_data__ =
                          scatterDataByApplicationKey?.[
                            getApplicationKey(serverMapCurrentTarget)
                          ]?.acc;
                        window.open(
                          `${BASE_PATH}${getTransactionListPath(
                            application,
                            searchParameters,
                          )}&${getTransactionListQueryString({
                            ...data,
                            checkedLegends: checkedLables,
                            agentId: currentServer?.agentId,
                          })}&withFilter=true`,
                        );
                      }
                    }}
                  />
                </div>
                <Separator />
              </>
            )}
          </ChartsBoard>
        </div>
      </Drawer>
    </>
  );
};
