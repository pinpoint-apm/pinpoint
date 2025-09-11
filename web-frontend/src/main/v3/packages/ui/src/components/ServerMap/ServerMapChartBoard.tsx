import React from 'react';
import { useTranslation } from 'react-i18next';
import {
  ChartBoardSkeleton,
  ErrorBoundary,
  ChartsBoard,
  ChartsBoardProps,
  Button,
  ChartTypeButtons,
  InstanceCount,
  ApdexScore,
  ScatterChart,
  Heatmap,
  Separator,
  MergedServerSearchList,
  MergedServerSearchListProps,
  Drawer,
  ScatterChartStatic,
  ChartsBoardHeader,
} from '@pinpoint-fe/ui/src/components';
import { Configuration, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import {
  useExperimentals,
  // useGetServerMapGetResponseTimeHistogramDataV2,
  useServerMapSearchParameters,
} from '@pinpoint-fe/ui/src/hooks';
import { MdArrowBackIosNew, MdArrowForwardIos } from 'react-icons/md';
import { PiArrowSquareOut } from 'react-icons/pi';
import {
  currentServerAtom,
  CurrentTarget,
  scatterDataAtom,
  serverMapChartTypeAtom,
  serverMapCurrentTargetAtom,
  serverMapCurrentTargetDataAtom,
  serverMapDataAtom,
} from '@pinpoint-fe/ui/src/atoms';
import { useAtom, useAtomValue } from 'jotai';
import { getServerImagePath } from '@pinpoint-fe/ui/src/utils';
import { cn } from '@pinpoint-fe/ui/src/lib';
import { RxChevronRight } from 'react-icons/rx';
import { ServerList } from '@pinpoint-fe/web/src/components/ServerList/ServerList';
import { useGetHistogramStatistics } from '@pinpoint-fe/ui/src/hooks/api/useGetHistogramStatistics';

export interface ServerMapChartsBoardProps extends ServerMapChartsBoardFetcherProps {}

export const ServerMapChartsBoard = ({ ...props }: ServerMapChartsBoardProps) => {
  return (
    <>
      <ErrorBoundary>
        <React.Suspense fallback={<ChartBoardSkeleton />}>
          <ServerMapChartsBoardFetcher {...props} />
        </React.Suspense>
      </ErrorBoundary>
    </>
  );
};

export interface ServerMapChartsBoardFetcherProps
  extends Omit<ChartsBoardProps, 'timestamp' | 'nodeData' | 'header'> {
  authorizationGuideUrl?: string;
  currentPanelWidth: number;
  SERVER_LIST_WIDTH: number;
  resizeHandleWidth: number;
  SERVERMAP_CONTAINER_ID: string;
  configuration?: Configuration;
}

export const ServerMapChartsBoardFetcher = ({
  authorizationGuideUrl,
  currentPanelWidth,
  SERVER_LIST_WIDTH,
  resizeHandleWidth,
  SERVERMAP_CONTAINER_ID,
  configuration,
  children,
  ...props
}: ServerMapChartsBoardFetcherProps) => {
  const { t } = useTranslation();
  const experimentalOption = useExperimentals();
  const { application, dateRange, pathname } = useServerMapSearchParameters();

  const chartType = useAtomValue(serverMapChartTypeAtom);
  const scatterData = useAtomValue(scatterDataAtom);

  const [openServerView, setOpenServerView] = React.useState(false);
  const [isScatterDataOutdated, setIsScatterDataOutdated] = React.useState(chartType !== 'scatter');
  const useStatisticsAgentState = experimentalOption.statisticsAgentState.value ?? true;

  // /api/servermap/serverMap 로 가져온 data
  const serverMapData = useAtomValue(serverMapDataAtom);
  // serverMap에서 클릭 된 target (node 또는 link)
  const [serverMapCurrentTarget, setServerMapCurrentTarget] = useAtom(serverMapCurrentTargetAtom);
  // 클릭 된 serverMap target의 data (serverMapCurrentTarget, serverMapData를 이용해 나온 값) serverMap이 클릭되었을 때 주는 node/link의 데이터가 충분치 않아서 사용하는 값
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom);
  // VIEW SERVERS로 열었을 때 왼쪽에 클릭된 서버
  const currentServer = useAtomValue(currentServerAtom);

  const { data, isLoading } = useGetHistogramStatistics({
    useStatisticsAgentState,
    nodeKey:
      (currentTargetData as GetServerMap.NodeData)?.nodeKey ||
      `${application?.applicationName}^${application?.serviceType}`, // 원래 optional인데 첫 페이지 로딩 시 currentTargetData가 없을 때, currentTargetData가 application으로 잡힐 때 두번 중복 call 방지를 위해 required 처럼 사용
    linkKey: (currentTargetData as GetServerMap.LinkData)?.linkKey,
  });

  React.useEffect(() => {
    setOpenServerView(false);
  }, [pathname]);

  React.useEffect(() => {
    if (
      chartType === 'scatter' ||
      (scatterData?.dateRange && scatterData?.dateRange[0] === dateRange.from?.getTime())
      // from, to 둘 다 비교해야하는데 정확한 to를 useGetScatterData가 주지 않음
    ) {
      setIsScatterDataOutdated(false);
      return;
    }

    setIsScatterDataOutdated(true);
  }, [dateRange, scatterData]);

  const getClickedMergedNodeList = ({ nodes, edges }: CurrentTarget) => {
    const nodeIds = nodes
      ? nodes.map((node) => node.id)
      : edges
        ? edges.map((edge) => edge.target)
        : [];

    return ((serverMapData?.applicationMapData.nodeDataArray as GetServerMap.NodeData[]) || [])
      .filter(({ key }: GetServerMap.NodeData) => nodeIds.includes(key))
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

  const shouldHideScatter = React.useCallback(() => {
    return currentTargetData && !(currentTargetData as GetServerMap.NodeData)?.isWas;
  }, [currentTargetData]);

  const timestamp = React.useMemo(() => {
    return data?.timeSeriesHistogram?.[0]?.values?.map((v) => v?.[0]);
  }, [data]);

  const histogramStatisticsData = React.useMemo(() => {
    if (!isLoading && data) {
      return {
        histogram: data?.histogram,
        responseStatistics: data?.responseStatistics,
        timeSeriesHistogram: data?.timeSeriesHistogram?.map((tsh) => {
          return {
            ...tsh,
            values: tsh.values.map((v) => v?.[1]),
          };
        }),
      };
    }

    return;
  }, [isLoading, data, currentTargetData]);

  const agentHistogramStatisticsData = React.useMemo(() => {
    if (!isLoading && data && currentServer?.agentId) {
      return {
        histogram: data?.agentHistogram?.[currentServer?.agentId || ''],
        responseStatistics: data?.agentResponseStatistics?.[currentServer?.agentId || ''],
        timeSeriesHistogram: data?.agentTimeSeriesHistogram?.[currentServer?.agentId || '']?.map(
          (tsh) => {
            return {
              ...tsh,
              values: tsh.values.map((v) => v?.[1]),
            };
          },
        ),
      };
    }

    return;
  }, [isLoading, data, currentServer?.agentId]);

  return (
    <>
      <ChartsBoard
        {...props}
        timestamp={timestamp}
        nodeData={
          (serverMapCurrentTarget && !currentTargetData) || // USER 처럼 merged node일 경우
          (currentTargetData as GetServerMap.NodeData)?.isAuthorized === false
            ? undefined
            : (histogramStatisticsData as GetServerMap.NodeData)
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
        (currentTargetData as GetServerMap.NodeData)?.isAuthorized ? (
          <>
            {serverMapCurrentTarget?.nodes || serverMapCurrentTarget?.edges ? (
              <MergedServerSearchList
                list={getClickedMergedNodeList(serverMapCurrentTarget)}
                onClickItem={handleClickMergedItem}
              />
            ) : (
              <>
                {serverMapCurrentTarget?.type === 'node' &&
                (currentTargetData as GetServerMap.NodeData)?.instanceCount ? (
                  <div className="flex items-center h-12 py-2.5 px-4 gap-2">
                    <Button
                      className="px-2 py-1 text-xs"
                      variant="outline"
                      onClick={() => setOpenServerView(!openServerView)}
                    >
                      {openServerView ? <MdArrowForwardIos /> : <MdArrowBackIosNew />}
                      <span className="ml-2">VIEW SERVERS</span>
                    </Button>
                    {!shouldHideScatter() && <ChartTypeButtons configuration={configuration} />}
                    <InstanceCount nodeData={data as unknown as GetServerMap.NodeData} />
                  </div>
                ) : !shouldHideScatter() ? (
                  <div className="flex items-center h-12 py-2.5 px-4 gap-2">
                    <ChartTypeButtons configuration={configuration} />
                  </div>
                ) : null}
                {!shouldHideScatter() && (
                  <>
                    <div
                      className={cn('w-full p-5', {
                        'mb-12 aspect-[1.618]': chartType === 'scatter',
                        'aspect-[1.4]': chartType === 'heatmap',
                      })}
                    >
                      <div className="h-7">
                        <ApdexScore
                          nodeData={(currentTargetData as GetServerMap.NodeData) || application}
                        />
                      </div>
                      {chartType === 'scatter' ? (
                        <ScatterChart
                          node={(serverMapCurrentTarget || application) as CurrentTarget}
                        />
                      ) : (
                        <Heatmap
                          nodeData={(currentTargetData as GetServerMap.NodeData) || application}
                        />
                      )}
                    </div>
                    <Separator />
                  </>
                )}
              </>
            )}
          </>
        ) : (
          <div className="flex justify-center pt-24 font-semibold text-status-fail">
            <a href={authorizationGuideUrl} target="_blank">
              You don't have authorization.
              {authorizationGuideUrl && <PiArrowSquareOut />}
            </a>
          </div>
        )}
      </ChartsBoard>
      <Drawer
        open={openServerView}
        getContainer={`#${SERVERMAP_CONTAINER_ID}`}
        contentWrapperStyle={{
          width: currentPanelWidth + SERVER_LIST_WIDTH,
          right: currentPanelWidth + resizeHandleWidth,
        }}
        onClose={() => setOpenServerView(false)}
      >
        <div style={{ width: SERVER_LIST_WIDTH }}>
          <div className="flex items-center h-12 gap-1 font-semibold border-b-1 shrink-0">
            <img src={serverMapCurrentTarget?.imgPath} width={52} />
            <div className="truncate">{serverMapCurrentTarget?.applicationName}</div>
          </div>
          <ServerList nodeStatistics={data} disableFetch={!openServerView} />
        </div>
        <div style={{ width: currentPanelWidth }}>
          <ChartsBoard
            header={
              <div className="flex items-center h-12 gap-1 font-semibold border-b-1 shrink-0">
                <div className="flex items-center">
                  <RxChevronRight />
                </div>
                {currentServer?.agentId}
              </div>
            }
            timestamp={timestamp}
            nodeData={agentHistogramStatisticsData as GetServerMap.NodeData}
            chartsContainerClassName="pt-12"
            emptyMessage={t('COMMON.NO_DATA')}
          >
            {!shouldHideScatter() && application && (
              <>
                <div className="w-full p-5 mb-12 aspect-[1.618] relative">
                  <div className="h-7">
                    {currentServer?.agentId && (
                      <ApdexScore
                        nodeData={currentTargetData as GetServerMap.NodeData}
                        agentId={currentServer?.agentId}
                      />
                    )}
                  </div>
                  <ScatterChartStatic
                    application={serverMapCurrentTarget!}
                    data={
                      isScatterDataOutdated ? [] : scatterData.acc[currentServer?.agentId || '']
                    }
                    range={[dateRange.from.getTime(), dateRange.to.getTime()]}
                    selectedAgentId={currentServer?.agentId || ''}
                  />
                  {isScatterDataOutdated && (
                    <div className="absolute top-0 left-0 z-[1000] flex flex-col items-center justify-center w-full h-[calc(100%+48px)] bg-background/50 text-center">
                      {t('SERVER_MAP.SCATTER_CHART_STATIC_WARN')
                        .split('\n')
                        .map((txt, i) => (
                          <p key={i}>{txt}</p>
                        ))}
                    </div>
                  )}
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
