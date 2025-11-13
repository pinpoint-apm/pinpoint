import React from 'react';
import { useTranslation } from 'react-i18next';
import {
  Node,
  Edge,
  ServerMap as ServerMapComponent,
  ServerMapProps as ServerMapComponentProps,
  MergedEdge,
} from '@pinpoint-fe/server-map';
import { FilteredMapType as FilteredMap, GetServerMap } from '@pinpoint-fe/ui/src/constants';
import { addCommas, getServerImagePath } from '@pinpoint-fe/ui/src/utils';
import {
  ServerMapMenu,
  SERVERMAP_MENU_CONTENT_TYPE,
  SERVERMAP_MENU_FUNCTION_TYPE,
  ServerMapMenuContent,
  ServerMapMenuItem,
} from './ServerMapMenu';
import { useOnClickOutside, useUpdateEffect } from 'usehooks-ts';
import { FaExternalLinkAlt } from 'react-icons/fa';
import { FaLocationCrosshairs, FaRotate } from 'react-icons/fa6';
import { FaGear } from 'react-icons/fa6';
import {
  Button,
  ErrorBoundary,
  ThrowError,
  Separator,
  ServerMapQueryOption,
  ServerMapQueryOptionProps,
  ServerMapSearchList,
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
  ServerMapSkeleton,
} from '..';
import cytoscape from 'cytoscape';

export interface ServerMapCoreProps extends Omit<ServerMapComponentProps, 'data'> {
  data?: GetServerMap.Response | FilteredMap.Response;
  isLoading?: boolean;
  error?: Error | null;
  onClickMenuItem?: (type: SERVERMAP_MENU_FUNCTION_TYPE, data: Node | Edge) => void;
  onMergeStateChange?: () => void;
  inputPlaceHolder?: string;
  queryOption?: ServerMapQueryOptionProps['queryOption'];
  onApplyChangedOption?: ServerMapQueryOptionProps['onApply'];
  disableMenu?: boolean;
}

export const ServerMapCore = ({
  data,
  isLoading,
  error,
  baseNodeId,
  onHoverNode,
  onClickNode,
  onClickEdge,
  onClickMenuItem,
  onMergeStateChange,
  onApplyChangedOption,
  queryOption,
  inputPlaceHolder,
  disableMenu,
  ...props
}: ServerMapCoreProps) => {
  const isEmpty = data?.applicationMapData?.nodeDataArray.length === 0;
  const { t } = useTranslation();
  const containerRef = React.useRef(null);
  const rightClickTargetRef = React.useRef<Node | Edge>();
  const hoverNodeRef = React.useRef<Node>();
  const [popperContentType, setPopperContentType] = React.useState<SERVERMAP_MENU_CONTENT_TYPE>();
  const [popperPosition, setPopperPosition] = React.useState<
    Partial<{
      x: number;
      y: number;
    }>
  >({
    x: 0,
    y: 0,
  });
  const allServiceTypes = React.useRef<string[]>();
  const popperContentRef = React.useRef(null);
  const [unCheckedServiceTypes, setUnCheckedServiceTypes] = React.useState<string[]>([]);
  const [checkedServiceTypes, setCheckedServiceTypes] = React.useState<string[]>([]);
  const cyRef = React.useRef<Parameters<NonNullable<ServerMapComponentProps['cy']>>[0]>();
  const [serverMapData, setServerMapData] = React.useState<{
    nodes: Node[];
    edges: Edge[];
  }>({
    nodes: [],
    edges: [],
  });

  useOnClickOutside(popperContentRef, () => {
    setPopperContentType(undefined);
  });

  React.useEffect(() => {
    const { nodeDataArray = [], linkDataArray = [] } = data?.applicationMapData || {};
    const nodeTypes = new Set(nodeDataArray.map((node) => node.serviceType));
    const isFilteredMap = 'lastFetchedTimestamp' in (data || {});

    allServiceTypes.current = Array.from(nodeTypes);

    const nodes = nodeDataArray.map((node) => {
      return {
        id: node.key,
        label: node.applicationName,
        type: node.serviceType,
        apdex: node.apdex,
        imgPath: getServerImagePath(node),
        transactionInfo: getTransactionInfo(node),
        timeSeriesApdexInfo: isFilteredMap ? undefined : getTimeSeriesApdexInfo(node), // filtered map에서는 시간 시리즈 Apdex 정보를 사용하지 않는다.
        shouldNotMerge: () => {
          return (
            node.nodeCategory === GetServerMap.NodeCategory.SERVER ||
            node.serviceType === 'USER' ||
            unCheckedServiceTypes.some((t) => t === node.serviceType)
          );
        },
      };
    });

    const edges = linkDataArray.map((link) => ({
      id: link.key,
      source: link.from,
      target: link.to,
      transactionInfo: {
        totalCount: link.totalCount,
        avgResponseTime: link.responseStatistics.Avg,
      },
    }));

    setServerMapData({ nodes, edges });
  }, [data, unCheckedServiceTypes]);

  useUpdateEffect(() => {
    onMergeStateChange?.();
  }, [unCheckedServiceTypes]);

  if (isLoading) {
    return <ServerMapSkeleton className="w-full h-full" />;
  }

  const getTransactionInfo = (node: GetServerMap.NodeData | FilteredMap.NodeData) => {
    const { nodeCategory, isAuthorized } = node;

    if (nodeCategory === GetServerMap.NodeCategory.SERVER && isAuthorized) {
      return {
        good: ['1s', '3s', '5s'].reduce((prev, curr) => {
          return prev + node?.histogram?.[curr as keyof GetServerMap.Histogram];
        }, 0),
        slow: node.histogram?.Slow,
        bad: node.histogram?.Error,
        instanceCount: node.instanceCount,
      };
    }
  };

  const getTimeSeriesApdexInfo = (node: GetServerMap.NodeData | FilteredMap.NodeData) => {
    const { isAuthorized, timeSeriesHistogram } = node;

    if (!isAuthorized || !timeSeriesHistogram) {
      return [];
    }

    const timestamp = data?.applicationMapData?.timestamp || [];
    const maxSlots = 24; // 전체 원을 최대 24개의 slot으로 나눈다.
    const dataLength = timestamp?.length;

    const oneSecond =
      timeSeriesHistogram?.find((time) => time.key === '1s' || time.key === '100ms')?.values || [];
    const threeSecond =
      timeSeriesHistogram?.find((time) => time.key === '3s' || time.key === '300ms')?.values || [];
    const total = timeSeriesHistogram?.find((time) => time.key === 'Tot')?.values || [];

    if (
      oneSecond.length !== dataLength ||
      threeSecond.length !== dataLength ||
      total.length !== dataLength
    ) {
      return [];
    }

    if (dataLength <= maxSlots) {
      return timestamp?.map((time, index) => {
        if (total?.[index] === 0) {
          return 1;
        }

        return (oneSecond?.[index] * 1 + threeSecond?.[index] * 0.5) / total?.[index];
      });
    }

    const groupSize = Math.floor(dataLength / maxSlots); // 각 slot에 최소 몇 개의 데이터씩 들어가는지
    let remainder = dataLength % maxSlots; // 나머지는 앞쪽 slot에 하나씩 더 넣기

    let i = 0;
    const result = [];

    while (i < dataLength) {
      const currentSlotSize = groupSize + (remainder > 0 ? 1 : 0);
      remainder = Math.max(0, remainder - 1); // 나머지 하나 줄이기

      const mergedTotal = total?.slice(i, i + currentSlotSize)?.reduce((t, acc) => {
        return acc + t;
      }, 0);

      if (mergedTotal === 0) {
        result.push(1);
      } else {
        const mergedOneSecond = oneSecond?.slice(i, i + currentSlotSize)?.reduce((t, acc) => {
          return acc + t;
        }, 0);
        const mergedThreeSecond = threeSecond?.slice(i, i + currentSlotSize)?.reduce((t, acc) => {
          return acc + t;
        }, 0);

        result.push((mergedOneSecond * 1 + mergedThreeSecond * 0.5) / mergedTotal);
      }
      i += currentSlotSize;
    }

    return result;
  };

  const handleClickBackground: ServerMapCoreProps['onClickBackground'] = ({
    eventType,
    position,
  }) => {
    if (eventType === 'right') {
      setPopperPosition(position);
      setPopperContentType(SERVERMAP_MENU_CONTENT_TYPE.BACKGROUND);
    }
  };

  const handleHoverNode: ServerMapCoreProps['onHoverNode'] = (params) => {
    const { eventType, position, data } = params;
    if (eventType === 'hover') {
      if (data && data?.apdex) {
        setPopperPosition(position);
        setPopperContentType(SERVERMAP_MENU_CONTENT_TYPE.HOVER_NODE);
        hoverNodeRef.current = data;
      } else {
        setPopperContentType(undefined);
        hoverNodeRef.current = undefined;
      }
    }
    onHoverNode?.(params);
  };

  const handleClickNode: ServerMapCoreProps['onClickNode'] = (params) => {
    const { eventType, position } = params;
    if (eventType === 'right' && params.data?.transactionInfo) {
      setPopperPosition(position);
      setPopperContentType(SERVERMAP_MENU_CONTENT_TYPE.NODE);
      rightClickTargetRef.current = params.data;
    }
    onClickNode?.(params);
  };

  const handleClickEdge: ServerMapCoreProps['onClickEdge'] = (params) => {
    const { eventType, position } = params;
    if (eventType === 'right' && params.data?.transactionInfo) {
      setPopperPosition(position);
      setPopperContentType(SERVERMAP_MENU_CONTENT_TYPE.EDGE);
      rightClickTargetRef.current = params.data;
    }
    onClickEdge?.(params);
  };

  const handleClickMenuItem = (type: SERVERMAP_MENU_FUNCTION_TYPE) => {
    onClickMenuItem?.(type, rightClickTargetRef.current!);
    setPopperContentType(undefined);
  };

  const handleClickSearchListItem = (item: GetServerMap.NodeData | FilteredMap.NodeData) => {
    const { key } = item;
    let clickedNode = cyRef.current?.getElementById(key);

    if (clickedNode?.empty()) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      clickedNode = cyRef.current?.nodes().filter((node: any) => {
        const { nodes } = node.data();

        return Boolean(nodes) && nodes.some(({ id }: Node) => id === key);
      });
    }

    cyRef.current!.center(clickedNode);
    clickedNode?.select();
    clickedNode?.emit('tap');
  };

  const reset = () => {
    const baseNode = cyRef.current?.getElementById(baseNodeId);
    cyRef.current?.zoom(1);
    cyRef.current
      ?.layout({
        name: 'dagre',
        fit: false,
        rankDir: 'LR',
        rankSep: 200,
      } as cytoscape.LayoutOptions)
      .run();

    cyRef.current?.$('node:selected').unselect();
    baseNode?.select();
    baseNode?.emit('tap');
    setPopperContentType(undefined);
  };

  const locateCurrentTarget = () => {
    const currentTargetId =
      cyRef.current?.$('node:selected').id() || cyRef.current?.$('edge:selected').id();

    if (currentTargetId) {
      cyRef.current?.center(cyRef.current.getElementById(currentTargetId));
    } else {
      reset();
    }
  };

  const renderApdexScore = () => {
    const totalSamples = hoverNodeRef?.current?.apdex?.apdexFormula?.totalSamples || 0;
    const satisfiedCount = hoverNodeRef?.current?.apdex?.apdexFormula?.satisfiedCount || 0;
    const toleratingCount = hoverNodeRef?.current?.apdex?.apdexFormula?.toleratingCount || 0;
    const frustratedCount = totalSamples - satisfiedCount - toleratingCount;

    return (
      <div className="flex flex-col gap-1 px-4 py-2 text-xs">
        <div className="flex items-center justify-between gap-2">
          Total<div className="font-semibold">{totalSamples}</div>
        </div>
        <div className="flex items-center justify-between gap-2">
          Satisfied<div className="font-semibold text-[#34b994]">{satisfiedCount}</div>
        </div>
        <div className="flex items-center justify-between gap-2">
          Tolerating<div className="font-semibold text-[#51afdf]">{toleratingCount}</div>
        </div>
        <div className="flex items-center justify-between gap-2">
          Frustrated<div className="font-semibold text-[#e95459]">{frustratedCount}</div>
        </div>
      </div>
    );
  };

  return (
    <div className="relative w-full h-full" ref={containerRef}>
      {isEmpty ? (
        <div className="flex items-center justify-center flex-1 w-full h-full text-muted-foreground">
          {t('COMMON.NO_AGENTS')}
        </div>
      ) : (
        <>
          <div className="absolute flex flex-col gap-2 top-3 right-4 z-[3] bg-white">
            <ServerMapSearchList
              inputPlaceHolder={inputPlaceHolder}
              list={data?.applicationMapData?.nodeDataArray}
              onClickItem={handleClickSearchListItem}
            />
            {onApplyChangedOption && (
              <ServerMapQueryOption queryOption={queryOption} onApply={onApplyChangedOption} />
            )}
          </div>
          <div className="flex absolute flex-col z-[3] gap-2 right-4 bottom-6">
            <TooltipProvider>
              <Tooltip>
                <TooltipTrigger asChild>
                  <Button
                    variant="outline"
                    className="flex w-12 h-12 p-2 text-lg bg-white"
                    onClick={locateCurrentTarget}
                  >
                    <FaLocationCrosshairs />
                  </Button>
                </TooltipTrigger>
                <TooltipContent side="left">
                  <p>Current Target</p>
                </TooltipContent>
              </Tooltip>
              <Tooltip>
                <TooltipTrigger asChild>
                  <Button
                    variant="outline"
                    className="flex w-12 h-12 p-2 text-lg bg-white"
                    onClick={reset}
                  >
                    <FaRotate />
                  </Button>
                </TooltipTrigger>
                <TooltipContent side="left">
                  <p>Reset</p>
                </TooltipContent>
              </Tooltip>
            </TooltipProvider>
          </div>
          {!disableMenu && (
            <ServerMapMenu contentType={popperContentType} position={popperPosition}>
              <div ref={popperContentRef}>
                {popperContentType === SERVERMAP_MENU_CONTENT_TYPE.BACKGROUND && (
                  <ServerMapMenuContent title="Merge">
                    {checkedServiceTypes.length === 0 && unCheckedServiceTypes.length === 0 ? (
                      <ServerMapMenuItem className="my-2 pointer-events-none text-muted-foreground">
                        There are no merged service types.
                      </ServerMapMenuItem>
                    ) : (
                      <>
                        {checkedServiceTypes.map((type) => (
                          <ServerMapMenuItem key={type}>
                            <label className="flex items-center flex-1 w-full gap-2 cursor-pointer">
                              <input
                                defaultChecked={true}
                                type="checkbox"
                                onClick={() => {
                                  setCheckedServiceTypes((prev) => prev.filter((t) => t !== type));
                                  setUnCheckedServiceTypes((prev) => [...prev, type]);
                                }}
                              />
                              <div className="truncate">{type}</div>
                            </label>
                          </ServerMapMenuItem>
                        ))}
                        {unCheckedServiceTypes
                          .filter((t) => allServiceTypes.current?.find((at) => at === t))
                          .map((type) => (
                            <ServerMapMenuItem key={type}>
                              <label className="flex items-center flex-1 w-full gap-2 cursor-pointer">
                                <input
                                  type="checkbox"
                                  onClick={() => {
                                    setCheckedServiceTypes((prev) => [...prev, type]);
                                    setUnCheckedServiceTypes((prev) =>
                                      prev.filter((t) => t !== type),
                                    );
                                  }}
                                />
                                <div className="truncate">{type}</div>
                              </label>
                            </ServerMapMenuItem>
                          ))}
                      </>
                    )}
                    <Separator />
                    <ServerMapMenuItem onClick={reset}>
                      <FaRotate style={{ fill: 'var(--primary)' }} />
                      Reset
                    </ServerMapMenuItem>
                  </ServerMapMenuContent>
                )}
                {popperContentType === SERVERMAP_MENU_CONTENT_TYPE.EDGE && (
                  <ServerMapMenuContent title={'Filter Transaction'}>
                    <ServerMapMenuItem
                      onClick={() =>
                        handleClickMenuItem(SERVERMAP_MENU_FUNCTION_TYPE.FILTER_TRANSACTION)
                      }
                    >
                      <FaExternalLinkAlt style={{ fill: 'var(--primary)' }} />
                      Open in new tab
                    </ServerMapMenuItem>
                    <Separator />
                    <ServerMapMenuItem
                      onClick={() =>
                        handleClickMenuItem(SERVERMAP_MENU_FUNCTION_TYPE.FILTER_WIZARD)
                      }
                    >
                      <FaGear style={{ fill: 'var(--primary)' }} />
                      Configures
                    </ServerMapMenuItem>
                  </ServerMapMenuContent>
                )}
                {popperContentType === SERVERMAP_MENU_CONTENT_TYPE.NODE && (
                  <ServerMapMenuContent title={'Filter Transaction'}>
                    <ServerMapMenuItem
                      onClick={() =>
                        handleClickMenuItem(SERVERMAP_MENU_FUNCTION_TYPE.FILTER_WIZARD)
                      }
                    >
                      <FaGear style={{ fill: 'var(--primary)' }} />
                      Configures
                    </ServerMapMenuItem>
                  </ServerMapMenuContent>
                )}
                {popperContentType === SERVERMAP_MENU_CONTENT_TYPE.HOVER_NODE && (
                  <ServerMapMenuContent title={'Apdex Score'}>
                    {renderApdexScore()}
                  </ServerMapMenuContent>
                )}
              </div>
            </ServerMapMenu>
          )}
          <ErrorBoundary>
            {error ? (
              <ThrowError error={error} />
            ) : (
              serverMapData.nodes.length > 0 && (
                <ServerMapComponent
                  baseNodeId={baseNodeId}
                  data={serverMapData}
                  renderNode={(node, transactionStatusSVGString) => {
                    return `
                  ${transactionStatusSVGString}
                  ${
                    node?.apdex?.apdexScore !== undefined &&
                    `<text 
                      x="50" y="80"
                      font-size="smaller"
                      dominant-baseline="middle"
                      text-anchor="middle"
                      font-family="Arial, Helvetica, sans-serif"
                    >${(Math.floor(node?.apdex?.apdexScore * 100) / 100).toFixed(2)}</text>`
                  }
                `;
                  }}
                  renderEdgeLabel={(edge: MergedEdge) => {
                    if (edge?.transactionInfo?.totalCount) {
                      return `${addCommas(edge?.transactionInfo?.totalCount)}${
                        edge.transactionInfo?.avgResponseTime
                          ? ` (${edge.transactionInfo.avgResponseTime} ms)`
                          : ''
                      }`;
                    } else if (edge?.edges) {
                      return `${edge.edges.reduce(
                        (acc, curr) => acc + curr.transactionInfo?.totalCount,
                        0,
                      )}`;
                    }
                    return '';
                  }}
                  onHoverNode={handleHoverNode}
                  onClickBackground={handleClickBackground}
                  onClickNode={handleClickNode}
                  onClickEdge={handleClickEdge}
                  onDataMerged={({ types }) => setCheckedServiceTypes(types)}
                  cy={(cy) => {
                    cyRef.current = cy;
                  }}
                  {...props}
                />
              )
            )}
          </ErrorBoundary>
        </>
      )}
    </div>
  );
};
