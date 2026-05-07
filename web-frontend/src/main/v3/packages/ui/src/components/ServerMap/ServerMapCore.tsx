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
import { addCommas, getServerImagePath, getTimeSeriesApdexInfo } from '@pinpoint-fe/ui/src/utils';
import {
  ServerMapMenu,
  SERVERMAP_MENU_CONTENT_TYPE,
  SERVERMAP_MENU_FUNCTION_TYPE,
  ServerMapMenuContent,
  ServerMapMenuItem,
} from './ServerMapMenu';
import { useOnClickOutside, useUpdateEffect } from 'usehooks-ts';
import { FaExternalLinkAlt, FaSearch } from 'react-icons/fa';
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
import { cn } from '../../lib';
import { Input } from '../ui/input';

export interface ServerMapCoreProps extends Omit<ServerMapComponentProps, 'data'> {
  data?: GetServerMap.Response | FilteredMap.Response;
  isLoading?: boolean;
  error?: Error | null;
  onClickMenuItem?: (type: SERVERMAP_MENU_FUNCTION_TYPE, data: Node | Edge) => void;
  onMergeStateChange?: () => void;
  onClickSubNode?: (subNode: GetServerMap.NodeData) => void;
  selectedSubNodeId?: string;
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
  onClickSubNode,
  selectedSubNodeId,
  onApplyChangedOption,
  queryOption,
  inputPlaceHolder,
  disableMenu,
  ...props
}: ServerMapCoreProps) => {
  const isEmpty = data?.applicationMapData?.nodeDataArray.length === 0;
  const { t } = useTranslation();
  const containerRef = React.useRef<HTMLDivElement>(null);
  const rightClickTargetRef = React.useRef<Node | Edge | undefined>(undefined);
  const hoverNodeRef = React.useRef<Node | undefined>(undefined);
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
  const allServiceTypes = React.useRef<string[] | undefined>(undefined);
  const popperContentRef = React.useRef<HTMLDivElement>(null);
  const [unCheckedServiceTypes, setUnCheckedServiceTypes] = React.useState<string[]>([]);
  const [checkedServiceTypes, setCheckedServiceTypes] = React.useState<string[]>([]);
  const cyRef = React.useRef<Parameters<NonNullable<ServerMapComponentProps['cy']>>[0] | undefined>(
    undefined,
  );
  const [serverMapData, setServerMapData] = React.useState<{
    nodes: Node[];
    edges: Edge[];
  }>({
    nodes: [],
    edges: [],
  });
  const serviceGroupTargetRef = React.useRef<GetServerMap.NodeData | undefined>(undefined);
  const [serviceGroupSearch, setServiceGroupSearch] = React.useState('');
  // cytoscape 이벤트 핸들러는 한 번 등록되어 popperContentType의 최신값을 stale closure로 놓친다.
  // 가드는 ref를 통해 항상 최신 상태를 본다.
  const popperContentTypeRef = React.useRef<SERVERMAP_MENU_CONTENT_TYPE | undefined>(undefined);
  React.useEffect(() => {
    popperContentTypeRef.current = popperContentType;
    if (popperContentType !== SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST) {
      setServiceGroupSearch('');
    }
  }, [popperContentType]);

  // service group 팝업이 열려 있는 동안 그래프 pan/zoom 시 팝업도 노드를 따라가도록 위치를 갱신한다.
  React.useEffect(() => {
    const cy = cyRef.current;
    if (popperContentType !== SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST || !cy) return;
    const targetId = serviceGroupTargetRef.current?.key;
    if (!targetId) return;

    const updatePosition = () => {
      const node = cy.getElementById(targetId);
      if (!node || node.empty()) return;
      const pos = node.renderedPosition();
      if (pos) setPopperPosition({ x: pos.x, y: pos.y });
    };

    updatePosition();
    cy.on('pan zoom layoutstop', updatePosition);
    return () => {
      cy.off('pan zoom layoutstop', updatePosition);
    };
  }, [popperContentType]);

  useOnClickOutside(popperContentRef as React.RefObject<HTMLDivElement>, () => {
    setPopperContentType(undefined);
  });

  React.useEffect(() => {
    const { nodeDataArray = [], linkDataArray = [] } = data?.applicationMapData || {};
    const nodeTypes = new Set(nodeDataArray.map((node) => node.serviceType));
    const isFilteredMap = 'lastFetchedTimestamp' in (data || {});

    allServiceTypes.current = Array.from(nodeTypes);

    const nodes: Node[] = nodeDataArray.map((node) => {
      const hasSubNodes = Array.isArray((node as GetServerMap.NodeData).subNodes);
      return {
        id: node.key,
        label: node.applicationName,
        type: node.serviceType,
        apdex: node.apdex,
        imgPath: getServerImagePath(node),
        transactionInfo: getTransactionInfo(node),
        timeSeriesApdexInfo: isFilteredMap
          ? undefined // filtered map에서는 시간 시리즈 Apdex 정보를 사용하지 않는다.
          : getTimeSeriesApdexInfo(node, data?.applicationMapData?.timestamp),
        shouldNotMerge: () => {
          // service group 노드(subNodes 보유)는 cytoscape의 자동 merge에 휘말리지 않도록 단독 표시한다.
          return (
            node.nodeCategory === GetServerMap.NodeCategory.SERVER ||
            node.serviceType === 'USER' ||
            hasSubNodes ||
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
    const { eventType, position, data, isLeftNode, target } = params;
    // service group 팝업이 열려 있는 동안에는 hover/unhover로 인해 팝업이 닫히거나 위치가 바뀌지 않도록 위임만 한다.
    if (popperContentTypeRef.current === SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST) {
      onHoverNode?.(params);
      return;
    }
    if (eventType === 'hover') {
      if (data && data?.apdex) {
        if (target) {
          setPopperPosition(
            isLeftNode
              ? {
                  x: target.renderedPosition()?.x + (target.renderedWidth() || 0) / 2,
                  y: target.renderedPosition()?.y,
                }
              : {
                  x: target.renderedPosition()?.x - (target.renderedWidth() || 0) - 160, // Popper content min width is 160px (min-w-40)
                  y: target.renderedPosition()?.y,
                },
          );
        } else {
          setPopperPosition(position);
        }

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
    const { eventType, position, data: clickedData } = params;
    if (eventType === 'right' && clickedData?.transactionInfo) {
      setPopperPosition(position);
      setPopperContentType(SERVERMAP_MENU_CONTENT_TYPE.NODE);
      rightClickTargetRef.current = clickedData;
    } else if (eventType === 'left' && clickedData) {
      const serviceGroup = (data?.applicationMapData?.nodeDataArray as
        | GetServerMap.NodeData[]
        | undefined)?.find(
        (n) => n.key === clickedData.id && Array.isArray(n.subNodes) && n.subNodes.length > 0,
      );
      if (serviceGroup) {
        setPopperPosition(position);
        setPopperContentType(SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST);
        serviceGroupTargetRef.current = serviceGroup;
      } else if (popperContentTypeRef.current !== SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST) {
        setPopperContentType(undefined);
      }
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
                {popperContentType === SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST &&
                  (() => {
                    const search = serviceGroupSearch.trim().toLowerCase();
                    const subNodes = serviceGroupTargetRef.current?.subNodes ?? [];
                    const filteredSubNodes = search
                      ? subNodes.filter((n) => n.applicationName?.toLowerCase().includes(search))
                      : subNodes;
                    return (
                      <ServerMapMenuContent
                        title={serviceGroupTargetRef.current?.applicationName ?? 'Service Group'}
                        onClose={() => setPopperContentType(undefined)}
                        className="w-64"
                      >
                        <div className="px-3 pb-2">
                          <div className="relative">
                            <FaSearch className="absolute -translate-y-1/2 pointer-events-none left-2 top-1/2 text-muted-foreground" />
                            <Input
                              autoFocus
                              placeholder={t('COMMON.SEARCH_INPUT')}
                              value={serviceGroupSearch}
                              onChange={(e) => setServiceGroupSearch(e.target.value)}
                              className="h-8 pl-7"
                            />
                          </div>
                        </div>
                        <div className="max-h-72 overflow-y-auto">
                          {filteredSubNodes.length === 0 ? (
                            <div className="px-3 py-2 text-muted-foreground">
                              {t('COMMON.EMPTY_ON_SEARCH')}
                            </div>
                          ) : (
                            filteredSubNodes.map((subNode) => {
                              const isSelected = subNode.key === selectedSubNodeId;
                              return (
                                <ServerMapMenuItem
                                  key={subNode.key}
                                  className={cn(isSelected && 'bg-accent font-semibold')}
                                  onClick={() => onClickSubNode?.(subNode)}
                                >
                                  <img src={getServerImagePath(subNode)} width={28} />
                                  <div className="truncate">{subNode.applicationName}</div>
                                </ServerMapMenuItem>
                              );
                            })
                          )}
                        </div>
                      </ServerMapMenuContent>
                    );
                  })()}
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
