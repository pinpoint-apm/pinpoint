import React from 'react';
import { useAtom, useAtomValue } from 'jotai';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  getServerMapPath,
  convertParamsToQueryString,
  getServerImagePath,
  getFilteredMapPath,
  getFilteredMapQueryString,
  // getV2RealtimeUrl,
  getApplicationKey,
  getFormattedDateRange,
  getRealtimePath,
} from '@pinpoint-fe/ui/src/utils';
import { useServerMapSearchParameters } from '@pinpoint-fe/ui/src/hooks';
import { MdArrowBackIosNew, MdArrowForwardIos } from 'react-icons/md';
import { ServerList } from '@pinpoint-fe/web/src/components/ServerList/ServerList';
import { RxChevronRight } from 'react-icons/rx';
import {
  serverMapDataAtom,
  serverMapCurrentTargetAtom,
  serverMapCurrentTargetDataAtom,
  currentServerAtom,
  scatterDataAtom,
  CurrentTarget,
} from '@pinpoint-fe/ui/src/atoms';
import {
  FilteredMapType as FilteredMap,
  GetServerMap,
  BASE_PATH,
  Configuration,
} from '@pinpoint-fe/ui/src/constants';
import { IoMdClose } from 'react-icons/io';
import {
  ApdexScore,
  Button,
  ChartsBoard,
  ChartsBoardHeader,
  DatetimePicker,
  DatetimePickerChangeHandler,
  Drawer,
  FilterWizard,
  getDefaultFilters,
  InstanceCount,
  MainHeader,
  MergedServerSearchList,
  MergedServerSearchListProps,
  ServerMap,
  ScatterChart,
  SERVERMAP_MENU_FUNCTION_TYPE,
  ScatterChartStatic,
  LayoutWithHorizontalResizable,
  Separator,
  ApplicationCombinedList,
  ServerChartsBoard,
  HelpPopover,
  ApplicationCombinedListProps,
  ChartTypeButtons,
  useServerMapChartType,
} from '@pinpoint-fe/ui';
import { Edge, Node } from '@pinpoint-fe/server-map';
import { PiTreeStructureDuotone, PiArrowSquareOut } from 'react-icons/pi';
import { Heatmap } from '@pinpoint-fe/ui/src/components/Heatmap';

export interface ServermapPageProps {
  authorizationGuideUrl?: string;
  configuration?: Configuration & Record<string, string>;
  ApplicationList?: (props: ApplicationCombinedListProps) => JSX.Element;
}

export const ServerMapPage = ({
  authorizationGuideUrl,
  configuration,
  ApplicationList = ApplicationCombinedList,
}: ServermapPageProps) => {
  const periodMax = configuration?.[`periodMax.serverMap`];
  const periodInterval = configuration?.[`periodInterval.serverMap`];
  const SERVERMAP_CONTAINER_ID = 'server-map-main-container';
  const containerRef = React.useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const { dateRange, application, searchParameters, queryOption, pathname } =
    useServerMapSearchParameters();
  const [serverMapCurrentTarget, setServerMapCurrentTarget] = useAtom(serverMapCurrentTargetAtom);
  const currentServer = useAtomValue(currentServerAtom);
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom);
  const serverMapData = useAtomValue(serverMapDataAtom);
  const [openServerView, setOpenServerView] = React.useState(false);
  const [openServerViewTransitionEnd, setServerViewTransitionEnd] = React.useState(false);
  const [showFilter, setShowFilter] = React.useState(false);
  const [filter, setFilter] = React.useState<FilteredMap.FilterState>();
  const [chartType] = useServerMapChartType();
  const [isScatterDataOutdated, setIsScatterDataOutdated] = React.useState(chartType !== 'scatter');
  const scatterData = useAtomValue(scatterDataAtom);
  const { t } = useTranslation();

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

  React.useEffect(() => {
    initPage();
  }, [pathname]);

  React.useEffect(() => {
    setShowFilter(false);

    if (
      serverMapData &&
      serverMapData?.applicationMapData?.nodeDataArray &&
      serverMapData?.applicationMapData?.nodeDataArray.length
    ) {
      let currentTarget: CurrentTarget;
      const isTargetIncluded =
        serverMapCurrentTarget &&
        ((serverMapData.applicationMapData.nodeDataArray as GetServerMap.NodeData[]).some(
          ({ key }) => key === serverMapCurrentTarget.id,
        ) ||
          (serverMapData.applicationMapData.linkDataArray as GetServerMap.LinkData[]).some(
            ({ key }) => key === serverMapCurrentTarget.id,
          ));

      if (isTargetIncluded || serverMapCurrentTarget?.nodes || serverMapCurrentTarget?.edges) {
        currentTarget = serverMapCurrentTarget;
        setServerMapCurrentTarget(currentTarget);
      } else {
        const applicationInfo = (
          serverMapData.applicationMapData.nodeDataArray as GetServerMap.NodeData[]
        ).find((node) => {
          return (
            getApplicationKey(application!) === node.key ||
            (node.applicationName === application?.applicationName &&
              node.serviceType === 'UNAUTHORIZED')
          );
        })!;

        if (applicationInfo) {
          const { applicationName, serviceType } = applicationInfo;
          currentTarget = {
            applicationName,
            serviceType,
            imgPath: getServerImagePath({ applicationName, serviceType }),
            type: 'node',
          };
          setServerMapCurrentTarget(currentTarget);
        }
      }
    } else {
      setServerMapCurrentTarget(undefined);
    }
  }, [serverMapData]);

  const shouldHideScatter = React.useCallback(() => {
    return currentTargetData && !(currentTargetData as GetServerMap.NodeData)?.isWas;
  }, [currentTargetData]);

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates: formattedDate, isRealtime }) => {
      if (isRealtime) {
        navigate(`${getRealtimePath(application!)}`);
      } else {
        navigate(
          `${getServerMapPath(application!)}?${convertParamsToQueryString({
            ...formattedDate,
            ...queryOption,
          })}`,
        );
      }
    }) as DatetimePickerChangeHandler,
    [application?.applicationName, queryOption],
  );

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

    return (serverMapData?.applicationMapData.nodeDataArray as GetServerMap.NodeData[])
      .filter(({ key }: GetServerMap.NodeData) => nodeIds.includes(key))
      .sort((node1, node2) => node2.totalCount - node1.totalCount);
  };

  const initPage = () => {
    setServerMapCurrentTarget(undefined);
    setOpenServerView(false);
    setShowFilter(false);
  };

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <PiTreeStructureDuotone />
            <div className="flex items-center gap-1">
              Servermap
              <HelpPopover helpKey="HELP_VIEWER.SERVER_MAP" />
            </div>
          </div>
        }
      >
        <ApplicationList
          open={!application}
          selectedApplication={application}
          onClickApplication={(application) => navigate(getServerMapPath(application))}
        />
        {application && (
          <div className="flex gap-1 ml-auto">
            <DatetimePicker
              enableRealtimeButton
              from={searchParameters.from}
              to={searchParameters.to}
              onChange={handleChangeDateRagePicker}
              maxDateRangeDays={periodMax}
              outOfDateRangeMessage={t('DATE_RANGE_PICKER.MAX_SEARCH_PERIOD', {
                maxSearchPeriod: periodMax,
              })}
              timeUnits={periodInterval}
            />
            <HelpPopover helpKey="HELP_VIEWER.NAVBAR" />
          </div>
        )}
      </MainHeader>
      {application && (
        <div
          id={SERVERMAP_CONTAINER_ID}
          className="relative flex-1 h-full overflow-x-hidden"
          ref={containerRef}
        >
          <LayoutWithHorizontalResizable
            withHandle={!openServerView}
            disabled={!serverMapCurrentTarget || openServerView}
          >
            <div className="relative w-full h-full">
              {application && (
                <>
                  {showFilter && (
                    <div className="absolute top-3 left-3 z-[1] bg-background rounded-lg shadow-lg border">
                      <button
                        className="absolute text-xl top-3 right-3 text-muted-foreground"
                        onClick={() => setShowFilter(false)}
                      >
                        <IoMdClose />
                      </button>
                      <FilterWizard
                        hideStatus={true}
                        tempFilter={filter}
                        openConfigures={true}
                        onClickApply={(filterStates) => {
                          const filterState = filterStates[filterStates.length - 1];
                          // eslint-disable-next-line @typescript-eslint/no-explicit-any
                          let addedHint = {} as any;
                          let soureIsWas;

                          if (!filterState.applicationName) {
                            const link = (
                              serverMapData?.applicationMapData
                                .linkDataArray as GetServerMap.LinkData[]
                            ).find(
                              (l) =>
                                l.key ===
                                `${filterState.fromApplication}^${filterState.fromServiceType}~${filterState.toApplication}^${filterState.toServiceType}`,
                            );
                            if (link) {
                              soureIsWas = link.sourceInfo.isWas;
                              addedHint =
                                link.sourceInfo.isWas && link.targetInfo.isWas
                                  ? {
                                      [link.targetInfo.applicationName]: link.filterTargetRpcList,
                                    }
                                  : {};
                            }
                          }

                          window.open(
                            `${BASE_PATH}${getFilteredMapPath(filterState, soureIsWas)}?from=${
                              searchParameters.from
                            }&to=${searchParameters.to}${getFilteredMapQueryString({
                              filterStates,
                              hint: {
                                addedHint,
                              },
                            })}
                              `,
                            '_blank',
                          );
                        }}
                      />
                    </div>
                  )}
                  <ServerMap
                    queryOption={queryOption}
                    onApplyChangedOption={(option) => {
                      navigate(
                        `${getServerMapPath(application)}?${convertParamsToQueryString({
                          ...getFormattedDateRange(dateRange),
                          ...option,
                        })}`,
                      );
                    }}
                    onClickMenuItem={(type, data) => {
                      if (type === SERVERMAP_MENU_FUNCTION_TYPE.FILTER_WIZARD) {
                        let serverInfos: Parameters<typeof getDefaultFilters>[1];
                        if ('type' in data) {
                          const nodeData = data as Node;
                          const node = (
                            serverMapData?.applicationMapData
                              .nodeDataArray as GetServerMap.NodeData[]
                          ).find((n) => n.key === nodeData.id);
                          serverInfos = {
                            agents: node?.agentIds,
                          };
                        } else if ('source' in data) {
                          const edgeData = data as Edge;
                          const link = (
                            serverMapData?.applicationMapData
                              .linkDataArray as GetServerMap.LinkData[]
                          ).find((l) => l.key === edgeData.id);
                          serverInfos = {
                            fromAgents: link?.fromAgent,
                            toAgents: link?.toAgent,
                          };
                        }
                        setShowFilter(true);
                        setFilter(getDefaultFilters(data, serverInfos));
                      } else if (type === SERVERMAP_MENU_FUNCTION_TYPE.FILTER_TRANSACTION) {
                        const defaultFilterState = getDefaultFilters(data);
                        const link = (
                          serverMapData?.applicationMapData.linkDataArray as GetServerMap.LinkData[]
                        ).find((l) => l.key === data.id);
                        const addedHint =
                          link?.sourceInfo.isWas && link.targetInfo.isWas
                            ? {
                                [link.targetInfo.applicationName]: link.filterTargetRpcList,
                              }
                            : // eslint-disable-next-line @typescript-eslint/no-explicit-any
                              ({} as any);

                        window.open(
                          `${BASE_PATH}${getFilteredMapPath(
                            defaultFilterState!,
                            link?.sourceInfo.isWas,
                          )}?from=${searchParameters.from}&to=${
                            searchParameters.to
                          }${getFilteredMapQueryString({
                            filterStates: [defaultFilterState!],
                            hint: {
                              addedHint,
                            },
                          })}
                                    `,
                          '_blank',
                        );
                      }
                    }}
                  />
                </>
              )}
            </div>
            {({ currentPanelWidth, SERVER_LIST_WIDTH, resizeHandleWidth }) => (
              <>
                <ChartsBoard
                  nodeData={
                    (currentTargetData as GetServerMap.NodeData)?.isAuthorized === false
                      ? undefined
                      : (currentTargetData as GetServerMap.NodeData)
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
                              <ChartTypeButtons />
                              <InstanceCount
                                nodeData={currentTargetData as GetServerMap.NodeData}
                              />
                            </div>
                          ) : !shouldHideScatter() ? (
                            <div className="flex items-center h-12 py-2.5 px-4 gap-2">
                              <ChartTypeButtons />
                            </div>
                          ) : null}
                          {!shouldHideScatter() && (
                            <>
                              {chartType === 'scatter' ? (
                                <div className="w-full p-5 mb-12 aspect-[1.618]">
                                  <div className="h-7">
                                    <ApdexScore
                                      nodeData={
                                        (currentTargetData as GetServerMap.NodeData) || application
                                      }
                                    />
                                  </div>
                                  <ScatterChart node={serverMapCurrentTarget || application} />
                                </div>
                              ) : (
                                <div className="w-full p-5 aspect-[1.3]">
                                  <Heatmap
                                    nodeData={
                                      (currentTargetData as GetServerMap.NodeData) || application
                                    }
                                  />
                                </div>
                              )}
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
                  afterOpenChange={(openChange) => setServerViewTransitionEnd(openChange)}
                  onClose={() => setOpenServerView(false)}
                >
                  <div style={{ width: SERVER_LIST_WIDTH }}>
                    <div className="flex items-center h-12 gap-1 font-semibold border-b-1 shrink-0">
                      <img src={serverMapCurrentTarget?.imgPath} width={52} />
                      <div className="truncate">{serverMapCurrentTarget?.applicationName}</div>
                    </div>
                    <ServerList disableFetch={!openServerView} />
                  </div>
                  <div style={{ width: currentPanelWidth }}>
                    <ServerChartsBoard
                      header={
                        <div className="flex items-center h-12 gap-1 font-semibold border-b-1 shrink-0">
                          <div className="flex items-center">
                            <RxChevronRight />
                          </div>
                          {currentServer?.agentId}
                        </div>
                      }
                      disableFetch={!openServerView && !openServerViewTransitionEnd}
                      nodeData={currentTargetData as GetServerMap.NodeData}
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
                                isScatterDataOutdated
                                  ? []
                                  : scatterData.acc[currentServer?.agentId || '']
                              }
                              range={[dateRange.from.getTime(), dateRange.to.getTime()]}
                              selectedAgentId={currentServer?.agentId || ''}
                            />
                            {isScatterDataOutdated && (
                              <div className="absolute top-0 left-0 z-[1000] flex items-center justify-center w-full h-[calc(100%+48px)] bg-background/50 text-center whitespace-normal break-words">
                                {t('SERVER_MAP.SCATTER_CHART_STATIC_WARN')}
                              </div>
                            )}
                          </div>
                          <Separator />
                        </>
                      )}
                    </ServerChartsBoard>
                  </div>
                </Drawer>
              </>
            )}
          </LayoutWithHorizontalResizable>
        </div>
      )}
    </div>
  );
};
