import React from 'react';
import { useAtom, useAtomValue } from 'jotai';
import { Link, useNavigate } from 'react-router-dom';
import {
  convertParamsToQueryString,
  getServerImagePath,
  getFilteredMapQueryString,
  getFilteredMapPath,
  getApplicationKey,
  getServerMapPath,
  getTransactionListPath,
  getTranscationListQueryString,
} from '@pinpoint-fe/ui/utils';
import { useFilteredMapParameters } from '@pinpoint-fe/ui/hooks';
import { MdArrowBackIosNew, MdArrowForwardIos } from 'react-icons/md';
import { ServerList } from '@/components/ServerList/ServerList';
import {
  serverMapDataAtom,
  serverMapCurrentTargetAtom,
  serverMapCurrentTargetDataAtom,
  currentServerAtom,
  scatterDataByApplicationKeyAtom,
  CurrentTarget,
} from '@pinpoint-fe/ui/atoms';
import {
  FilteredMapType as FilteredMap,
  GetServerMap,
  SCATTER_DATA_TOTAL_KEY,
  BASE_PATH,
} from '@pinpoint-fe/ui/constants';
import {
  ApplicationCombinedList,
  FilteredMap as FilteredMapComponent,
  LayoutWithHorizontalResizable,
  Separator,
  ServerChartsBoard,
  withInitialFetch,
} from '@pinpoint-fe/ui';
import { differenceInMinutes } from 'date-fns';
import { useUpdateEffect } from 'usehooks-ts';
import { useTranslation } from 'react-i18next';
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
  ProgressBarWithControls,
  SERVERMAP_MENU_FUNCTION_TYPE,
  ScatterChartStatic,
} from '@pinpoint-fe/ui';
import { getLayoutWithSideNavigation } from '@/components/Layout/LayoutWithSideNavigation';
import { Node, Edge } from '@pinpoint-fe/server-map';
import { PiTreeStructureDuotone } from 'react-icons/pi';

export interface FilteredMapPageProps {}

export const FilteredMapPage = ({}: FilteredMapPageProps) => {
  const containerRef = React.useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const { dateRange, application, parsedFilters, parsedHint, searchParameters, search } =
    useFilteredMapParameters();
  const [serverMapCurrentTarget, setServerMapCurrentTarget] = useAtom(serverMapCurrentTargetAtom);
  const currentServer = useAtomValue(currentServerAtom);
  const currentTargetData = useAtomValue(serverMapCurrentTargetDataAtom);
  const [serverMapData, setServerMapData] = useAtom(serverMapDataAtom);
  const [openServerView, setOpenServerView] = React.useState(false);
  const [openServerViewTransitionEnd, setServerViewTransitionEnd] = React.useState(false);
  const [appliedFilters, setAppliedFilters] =
    React.useState<FilteredMap.FilterState[]>(parsedFilters);
  const [filter, setFilter] = React.useState<FilteredMap.FilterState>();
  const [showFilterConfig, setShowFilterConfig] = React.useState(false);
  const [scatterDataByApplicationKey, setScatterDataByApplicationKey] = useAtom(
    scatterDataByApplicationKeyAtom,
  );
  const [pauseFilteredMapFetcher, setPauseFilteredMapFetcher] = React.useState(false);
  const { t } = useTranslation();

  useUpdateEffect(() => {
    setServerMapData(undefined);
    setScatterDataByApplicationKey(undefined);
    setPauseFilteredMapFetcher(false);
  }, [search]);

  React.useEffect(() => {
    setAppliedFilters((prev) => {
      return prev.map((prevFilter) => {
        if (prevFilter.applicationName && prevFilter.serviceType) {
          return {
            ...prevFilter,
            agents: (
              serverMapData?.applicationMapData.nodeDataArray as FilteredMap.NodeData[]
            )?.find((n) => n.key === `${prevFilter.applicationName}^${prevFilter.serviceType}`)
              ?.agentIds,
          };
        } else if (
          prevFilter.fromApplication &&
          prevFilter.fromServiceType &&
          prevFilter.toApplication &&
          prevFilter.toServiceType
        ) {
          const linkData = (
            serverMapData?.applicationMapData.linkDataArray as FilteredMap.LinkData[]
          )?.find(
            (n) =>
              n.key ===
              `${prevFilter.fromApplication}^${prevFilter.fromServiceType}~${prevFilter.toApplication}^${prevFilter.toServiceType}`,
          );

          return {
            ...prevFilter,
            fromAgents: linkData?.fromAgent,
            toAgents: linkData?.toAgent,
          };
        }
        return prevFilter;
      });
    });

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

  React.useEffect(() => {
    return () => {
      setScatterDataByApplicationKey(undefined);
    };
  }, []);

  const shouldHideScatter = () => {
    if (!currentTargetData) {
      return true;
    }
    return !(
      (currentTargetData && (currentTargetData as FilteredMap.NodeData)?.isWas)
      // && !currentTargetData?.isMerged
    );
  };

  const handleChangeDateRagePicker = React.useCallback(
    (({ formattedDates }) => {
      if (formattedDates) {
        navigate(
          `${getFilteredMapPath(
            parsedFilters[parsedFilters.length - 1],
          )}?${convertParamsToQueryString(formattedDates)}&${convertParamsToQueryString({
            filter: searchParameters.filter,
            hint: searchParameters.hint,
          })}`,
        );
      }
    }) as DatetimePickerChangeHandler,
    [application?.applicationName, searchParameters.filter, searchParameters.hint],
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

  return (
    <div className="flex flex-col flex-1 h-full">
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <PiTreeStructureDuotone />
            <Link className="hover:underline" to={getServerMapPath(application)}>
              Servermap
            </Link>{' '}
            / Filtered
          </div>
        }
      >
        <ApplicationCombinedList selectedApplication={application} disabled />
        <div className="ml-auto">
          {application && (
            <DatetimePicker
              from={searchParameters.from}
              to={searchParameters.to}
              onChange={handleChangeDateRagePicker}
              outOfDateRangeMessage={t('DATE_RANGE_PICKER.MAX_SEARCH_PERIOD', {
                maxSearchPeriod: 2,
              })}
            />
          )}
        </div>
      </MainHeader>
      {application && (
        <div
          id="server-map-main-container"
          className="relative flex-1 h-full overflow-x-hidden"
          ref={containerRef}
        >
          <LayoutWithHorizontalResizable
            withHandle={!openServerView}
            disabled={!serverMapCurrentTarget || openServerView}
          >
            <div className="relative flex flex-col w-full h-full gap-4">
              {application && (
                <>
                  <div className="absolute top-3 left-3 z-[1] bg-background rounded-lg shadow-lg border">
                    <FilterWizard
                      appliedFilters={appliedFilters}
                      tempFilter={filter}
                      openConfigures={showFilterConfig}
                      onClickShowConfig={() => setShowFilterConfig(!showFilterConfig)}
                      onClickApply={(filterStates) => {
                        const filterState = filterStates[filterStates.length - 1];
                        // eslint-disable-next-line @typescript-eslint/no-explicit-any
                        let addedHint = {} as any;
                        let soureIsWas;

                        if (!filterState.applicationName) {
                          const link = (
                            serverMapData?.applicationMapData
                              .linkDataArray as FilteredMap.LinkData[]
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
                              currHint: parsedHint,
                              addedHint,
                            },
                          })}
                              `,
                          '_blank',
                        );
                      }}
                    />
                  </div>
                  <ProgressBarWithControls
                    className="relative z-[1] top-3 left-90 w-[calc(100%-23.5rem)] shadow border"
                    progress={(serverMapData as FilteredMap.Response)?.lastFetchedTimestamp}
                    range={[dateRange.to.getTime(), dateRange.from.getTime()]}
                    tickCount={
                      differenceInMinutes(dateRange.to, dateRange.from) < 5
                        ? differenceInMinutes(dateRange.to, dateRange.from)
                        : 5
                    }
                    onClickPause={() => setPauseFilteredMapFetcher(true)}
                    onClickResume={() => setPauseFilteredMapFetcher(false)}
                  />
                  <FilteredMapComponent
                    isPaused={pauseFilteredMapFetcher}
                    onClickMenuItem={(type, data) => {
                      if (type === SERVERMAP_MENU_FUNCTION_TYPE.FILTER_WIZARD) {
                        let serverInfos: Parameters<typeof getDefaultFilters>[1];
                        if ('type' in data) {
                          const nodeData = data as Node;
                          const node = (
                            serverMapData?.applicationMapData
                              .nodeDataArray as FilteredMap.NodeData[]
                          ).find((n) => n.key === nodeData.id);
                          serverInfos = {
                            agents: node?.agentIds,
                          };
                        } else if ('source' in data) {
                          const edgeData = data as Edge;
                          const link = (
                            serverMapData?.applicationMapData
                              .linkDataArray as FilteredMap.LinkData[]
                          ).find((l) => l.key === edgeData.id);
                          serverInfos = {
                            fromAgents: link?.fromAgent,
                            toAgents: link?.toAgent,
                          };
                        }
                        setFilter(getDefaultFilters(data, serverInfos));
                        setShowFilterConfig(true);
                      } else if (type === SERVERMAP_MENU_FUNCTION_TYPE.FILTER_TRANSACTION) {
                        const defaultFilterState = getDefaultFilters(data);
                        const link = (
                          serverMapData?.applicationMapData.linkDataArray as FilteredMap.LinkData[]
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
                            filterStates: [...parsedFilters!, defaultFilterState!],
                            hint: {
                              currHint: parsedHint,
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
            {({ currentPanelWidth, SERVER_LIST_WIDTH, resizeHandleWidth }) =>
              serverMapCurrentTarget && (
                <>
                  <ChartsBoard
                    nodeData={
                      (currentTargetData as FilteredMap.NodeData)?.isAuthorized === false
                        ? undefined
                        : (currentTargetData as FilteredMap.NodeData)
                    }
                    header={
                      <ChartsBoardHeader
                        currentTarget={openServerView ? null : serverMapCurrentTarget}
                      />
                    }
                    emptyMessage={t('COMMON.NO_DATA')}
                  >
                    {serverMapCurrentTarget.nodes ||
                    serverMapCurrentTarget.edges ||
                    currentTargetData === undefined ||
                    serverMapCurrentTarget.type === 'edge' ||
                    (currentTargetData as GetServerMap.NodeData).isAuthorized ? (
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
                              <div className="flex items-center h-12 py-2.5 px-4">
                                <Button
                                  className="px-2 py-1 text-xs"
                                  variant="outline"
                                  onClick={() => setOpenServerView(!openServerView)}
                                >
                                  {openServerView ? <MdArrowForwardIos /> : <MdArrowBackIosNew />}
                                  <span className="ml-2">VIEW SERVERS</span>
                                </Button>
                                <InstanceCount
                                  nodeData={currentTargetData as GetServerMap.NodeData}
                                />
                              </div>
                            ) : null}
                            {!shouldHideScatter() && application && (
                              <>
                                <div className="w-full p-5 mb-12 aspect-[1.618]">
                                  <div className="h-7">
                                    <ApdexScore
                                      nodeData={currentTargetData as GetServerMap.NodeData}
                                    />
                                  </div>
                                  <ScatterChartStatic
                                    application={serverMapCurrentTarget!}
                                    data={
                                      scatterDataByApplicationKey?.[
                                        getApplicationKey(serverMapCurrentTarget)
                                      ]?.acc[SCATTER_DATA_TOTAL_KEY]
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
                                          )}&${getTranscationListQueryString({
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
                        You don't have authorization.
                      </div>
                    )}
                  </ChartsBoard>
                  <Drawer
                    open={openServerView}
                    getContainer={'#server-map-main-container'}
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
                            <div className="flex items-center justify-center">
                              <MdArrowForwardIos />
                            </div>
                            {currentServer?.agentId}
                          </div>
                        }
                        disableFetch={!openServerView && !openServerViewTransitionEnd}
                        nodeData={currentTargetData as GetServerMap.NodeData}
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
                                    ? scatterDataByApplicationKey?.[
                                        getApplicationKey(serverMapCurrentTarget)
                                      ]?.acc[currentServer?.agentId]
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
                                      )}&${getTranscationListQueryString({
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
                      </ServerChartsBoard>
                    </div>
                  </Drawer>
                </>
              )
            }
          </LayoutWithHorizontalResizable>
        </div>
      )}
    </div>
  );
};

export default withInitialFetch((props: FilteredMapPageProps) =>
  getLayoutWithSideNavigation(<FilteredMapPage {...props} />),
);
