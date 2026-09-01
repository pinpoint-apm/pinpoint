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
import {
  addCommas,
  buildServerMapSearchList,
  findServiceGroupLink,
  findServiceGroupNode,
  getServerImagePath,
  getTimeSeriesApdexInfo,
  ServerMapSearchItem,
} from '@pinpoint-fe/ui/src/utils';
import {
  ServerMapMenu,
  SERVERMAP_MENU_CONTENT_TYPE,
  SERVERMAP_MENU_FUNCTION_TYPE,
  ServerMapMenuContent,
  ServerMapMenuItem,
} from './ServerMapMenu';
import { useOnClickOutside } from 'usehooks-ts';
import { useUpdateEffect } from '@pinpoint-fe/ui/src/hooks';
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

/** 팝업이 map 영역 가장자리에 닿지 않도록 두는 여백(px) */
const POPPER_EDGE_GAP = 8;

export interface ServerMapCoreProps extends Omit<ServerMapComponentProps, 'data'> {
  data?: GetServerMap.Response | FilteredMap.Response;
  isLoading?: boolean;
  error?: Error | null;
  onClickMenuItem?: (type: SERVERMAP_MENU_FUNCTION_TYPE, data: Node | Edge) => void;
  onMergeStateChange?: () => void;
  onClickSubNode?: (subNode: GetServerMap.NodeData) => void;
  selectedSubNodeId?: string;
  onClickSubLink?: (subLink: GetServerMap.LinkData) => void;
  selectedSubLinkId?: string;
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
  onClickSubLink,
  selectedSubLinkId,
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
  const serviceGroupLinkTargetRef = React.useRef<GetServerMap.LinkData | undefined>(undefined);
  const [serviceGroupSearch, setServiceGroupSearch] = React.useState('');
  const serviceGroupSearchRef = React.useRef<HTMLInputElement>(null);
  // 팝업이 map 영역을 넘칠 때만 채워지는 대체 좌표. 넘치지 않으면 undefined로 두고
  // popperPosition을 그대로 쓴다 — 팝업이 열려 있는 동안 pan/zoom마다 좌표가 갱신되는데,
  // 그때마다 상태를 새로 쓰면 이동 한 번에 렌더가 두 번씩 돈다.
  const [clampedPosition, setClampedPosition] = React.useState<{ x: number; y: number }>();
  const menuPosition = clampedPosition ?? popperPosition;
  // cytoscape 이벤트 핸들러는 한 번 등록되어 popperContentType의 최신값을 stale closure로 놓친다.
  // 가드는 ref를 통해 항상 최신 상태를 본다.
  const popperContentTypeRef = React.useRef<SERVERMAP_MENU_CONTENT_TYPE | undefined>(undefined);
  React.useEffect(() => {
    popperContentTypeRef.current = popperContentType;
    if (
      popperContentType !== SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST &&
      popperContentType !== SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LINK_LIST
    ) {
      setServiceGroupSearch('');
    }
  }, [popperContentType]);

  /**
   * 팝업을 map 영역 안쪽으로 접어 넣는다.
   *
   * 팝업은 노드의 렌더 좌표에 절대 위치로 그려지므로, 오른쪽/아래 가장자리의 노드에서 열면 map
   * 컨테이너를 넘친다. 넘치면 조상(overflow hidden)의 scrollWidth가 늘어나고, 그 상태에서 검색
   * 입력에 포커스가 가면 브라우저가 입력을 보이게 하려고 scrollLeft를 옮긴다. 그러면 map 전체가
   * 왼쪽으로 밀려 사이드 네비게이션 아래로 들어간다.
   *
   * 넘칠 때만 기준점 반대편으로 뒤집고(popper의 flip), 그래도 모자라면 가장자리에 맞춘다.
   * 넘치지 않는 경우에는 기존 위치 그대로다.
   */
  React.useLayoutEffect(() => {
    const container = containerRef.current;
    // popperContentRef는 테두리를 그리는 래퍼 안에 있으므로, 실제 팝업 크기는 부모에서 잰다.
    const content = popperContentRef.current?.parentElement ?? popperContentRef.current;

    // 값이 그대로면 이전 참조를 돌려줘 리렌더를 만들지 않는다.
    const update = (next?: { x: number; y: number }) =>
      setClampedPosition((prev) => {
        if (!next) {
          return undefined;
        }
        return prev && prev.x === next.x && prev.y === next.y ? prev : next;
      });

    if (!popperContentType || !container || !content) {
      update();
      return;
    }

    const { width, height } = content.getBoundingClientRect();
    const fit = (start: number, size: number, limit: number) => {
      const flipped = start + size + POPPER_EDGE_GAP > limit ? start - size : start;
      const max = Math.max(POPPER_EDGE_GAP, limit - size - POPPER_EDGE_GAP);
      return Math.min(Math.max(flipped, POPPER_EDGE_GAP), max);
    };

    const x = fit(popperPosition.x ?? 0, width, container.clientWidth);
    const y = fit(popperPosition.y ?? 0, height, container.clientHeight);

    // 넘치지 않으면 popperPosition을 그대로 쓰면 되므로 대체 좌표를 두지 않는다.
    update(x === popperPosition.x && y === popperPosition.y ? undefined : { x, y });
  }, [popperContentType, popperPosition.x, popperPosition.y]);

  // service group 팝업의 검색 입력에 포커스를 준다. autoFocus를 쓰지 않는 이유는 위 클램프 주석과
  // 같다 — 팝업이 조금이라도 넘친 순간에 포커스가 가면 브라우저가 화면을 스크롤해 버린다.
  React.useEffect(() => {
    if (
      popperContentType === SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST ||
      popperContentType === SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LINK_LIST
    ) {
      serviceGroupSearchRef.current?.focus({ preventScroll: true });
    }
  }, [popperContentType]);

  // service group 팝업이 열려 있는 동안 그래프 pan/zoom 시 팝업도 노드/엣지를 따라가도록 위치를 갱신한다.
  React.useEffect(() => {
    const cy = cyRef.current;
    if (!cy) return;
    const isNodePopup = popperContentType === SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST;
    const isLinkPopup = popperContentType === SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LINK_LIST;
    if (!isNodePopup && !isLinkPopup) return;
    const targetId = isNodePopup
      ? serviceGroupTargetRef.current?.key
      : serviceGroupLinkTargetRef.current?.key;
    if (!targetId) return;

    const updatePosition = () => {
      const target = cy.getElementById(targetId);
      if (!target || target.empty()) return;
      if (isNodePopup) {
        const pos = target.renderedPosition();
        if (pos) setPopperPosition({ x: pos.x, y: pos.y });
      } else {
        // 엣지는 renderedPosition()이 없어 renderedBoundingBox의 중점을 사용한다.
        const bb = target.renderedBoundingBox();
        if (bb) setPopperPosition({ x: (bb.x1 + bb.x2) / 2, y: (bb.y1 + bb.y2) / 2 });
      }
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
      const subNodes = (node as GetServerMap.NodeData).subNodes;
      const hasSubNodes = Array.isArray(subNodes);
      return {
        id: node.key,
        label: node.applicationName,
        type: node.serviceType,
        apdex: node.apdex,
        imgPath: hasSubNodes ? '' : getServerImagePath(node),
        subNodesCount: hasSubNodes ? subNodes!.length : undefined,
        transactionInfo: getTransactionInfo(node),
        timeSeriesApdexInfo: isFilteredMap
          ? undefined // filtered map에서는 시간 시리즈 Apdex 정보를 사용하지 않는다.
          : getTimeSeriesApdexInfo(node),
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

  // 검색 목록. service group 노드는 그 자체와 소속 application을 모두 담아, service에 묶인
  // application도 이름으로 찾을 수 있게 한다.
  const searchList = React.useMemo(
    () =>
      buildServerMapSearchList(
        data?.applicationMapData?.nodeDataArray as GetServerMap.NodeData[] | undefined,
      ),
    [data],
  );

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
    if (
      popperContentTypeRef.current === SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST ||
      popperContentTypeRef.current === SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LINK_LIST
    ) {
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
    const serviceGroup = findServiceGroupNode(
      data?.applicationMapData?.nodeDataArray as GetServerMap.NodeData[] | undefined,
      clickedData?.id,
    );

    // service group 노드는 필터 메뉴를 열지 않는다. 기준 application이 없어 filteredMap 조회가
    // 성립하지 않으므로(`getFilterTargetApplication`이 null) 메뉴를 띄워봐야 눌러도 아무 일이
    // 일어나지 않는다. transactionInfo가 없는 노드에 메뉴를 띄우지 않는 것과 같은 처리다.
    if (eventType === 'right' && clickedData?.transactionInfo && !serviceGroup) {
      setPopperPosition(position);
      setPopperContentType(SERVERMAP_MENU_CONTENT_TYPE.NODE);
      rightClickTargetRef.current = clickedData;
    } else if (eventType === 'left' && clickedData) {
      if (serviceGroup) {
        setPopperPosition(position);
        setPopperContentType(SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST);
        serviceGroupTargetRef.current = serviceGroup;
      } else if (
        popperContentTypeRef.current !== SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST &&
        popperContentTypeRef.current !== SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LINK_LIST
      ) {
        setPopperContentType(undefined);
      }
    }
    onClickNode?.(params);
  };

  const handleClickEdge: ServerMapCoreProps['onClickEdge'] = (params) => {
    const { eventType, position, data: clickedData } = params;
    const serviceGroupLink = findServiceGroupLink(
      data?.applicationMapData?.linkDataArray as GetServerMap.LinkData[] | undefined,
      clickedData?.id,
    );

    // service group 링크(Application→Service, Service→Application, Service→Service)는 필터
    // 메뉴를 열지 않는다. 한쪽 끝에 application이 없어 필터가 반쪽만 걸린 filteredMap이 열리기
    // 때문이다. 노드와 같은 이유이고, filteredMap으로 연결되는 것은 Application→Application뿐이다.
    if (eventType === 'right' && clickedData?.transactionInfo && !serviceGroupLink) {
      setPopperPosition(position);
      setPopperContentType(SERVERMAP_MENU_CONTENT_TYPE.EDGE);
      rightClickTargetRef.current = clickedData;
    } else if (eventType === 'left' && clickedData) {
      if (serviceGroupLink) {
        setPopperPosition(position);
        setPopperContentType(SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LINK_LIST);
        serviceGroupLinkTargetRef.current = serviceGroupLink;
      } else if (
        popperContentTypeRef.current !== SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST &&
        popperContentTypeRef.current !== SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LINK_LIST
      ) {
        setPopperContentType(undefined);
      }
    }
    onClickEdge?.(params);
  };

  const handleClickMenuItem = (type: SERVERMAP_MENU_FUNCTION_TYPE) => {
    onClickMenuItem?.(type, rightClickTargetRef.current!);
    setPopperContentType(undefined);
  };

  /**
   * 그래프에 그려진 service group 노드 위에 자식 application 목록 팝업을 띄운다.
   * group 노드는 merge 대상이 아니므로(shouldNotMerge) 항상 자기 key로 그려져 있다.
   */
  const openServiceGroupList = (group: GetServerMap.NodeData) => {
    serviceGroupTargetRef.current = group;

    const target = cyRef.current?.getElementById(group.key);
    const position = target && !target.empty() ? target.renderedPosition() : undefined;

    if (position) {
      setPopperPosition({ x: position.x, y: position.y });
    }
    setPopperContentType(SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LIST);
  };

  /**
   * 검색 목록에서 항목을 고르면 그 노드로 이동한다.
   *
   * service group(접힌 service)에 묶인 자식 application은 그래프에 노드가 없다 — 그려진 것은
   * group 하나뿐이다. 그래서 센터링·선택은 group 노드에 하고, 자식 목록 팝업을 열어 무엇을
   * 골랐는지 보여준 뒤 조회 대상만 그 application으로 넘긴다(group 노드 좌클릭 → 목록에서
   * 자식 선택과 같은 상태다).
   */
  const handleClickSearchListItem = ({ node, serviceGroup }: ServerMapSearchItem) => {
    const key = serviceGroup?.key ?? node.key;
    let clickedNode = cyRef.current?.getElementById(key);

    if (clickedNode?.empty()) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      clickedNode = cyRef.current?.nodes().filter((n: any) => {
        const { nodes } = n.data();

        return Boolean(nodes) && nodes.some(({ id }: Node) => id === key);
      });
    }

    cyRef.current!.center(clickedNode);
    clickedNode?.select();
    clickedNode?.emit('tap');

    // group 노드 자체를 골랐을 때도 좌클릭과 같이 자식 목록을 펼친다. group은 그 자체로 조회
    // 대상이 될 수 없어(ServiceMapFetcher가 선택으로 만들지 않는다) 목록을 열지 않으면 노드만
    // 가운데로 오고 아무 일도 일어나지 않은 화면이 된다.
    const group =
      serviceGroup ??
      findServiceGroupNode(
        data?.applicationMapData?.nodeDataArray as GetServerMap.NodeData[] | undefined,
        node.key,
      );

    if (group) {
      openServiceGroupList(group);
    }
    if (serviceGroup) {
      onClickSubNode?.(node);
    }
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
              list={searchList}
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
            <ServerMapMenu contentType={popperContentType} position={menuPosition}>
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
                      // w-max는 가장 긴 application 이름만큼 팝업을 넓힌다. 상한을 두지 않으면
                      // 이름이 긴 노드에서 팝업이 map 컨테이너를 넘고, 그 상태로 검색 입력에
                      // 포커스가 가면 브라우저가 map을 왼쪽으로 스크롤한다(위 클램프 주석 참고).
                      <ServerMapMenuContent
                        title={serviceGroupTargetRef.current?.applicationName ?? 'Service Group'}
                        onClose={() => setPopperContentType(undefined)}
                        className="w-max min-w-72 max-w-[22.5rem]"
                      >
                        <div className="px-3 pb-2">
                          <div className="relative">
                            <FaSearch className="absolute -translate-y-1/2 pointer-events-none left-2 top-1/2 text-muted-foreground" />
                            <Input
                              ref={serviceGroupSearchRef}
                              placeholder={t('COMMON.SEARCH_INPUT')}
                              value={serviceGroupSearch}
                              onChange={(e) => setServiceGroupSearch(e.target.value)}
                              className="h-8 pl-7"
                            />
                          </div>
                        </div>
                        <div className="overflow-y-auto max-h-72">
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
                                  {/* 아이콘은 바로 옆 이름이 말해 주는 것을 되풀이할 뿐이라
                                      스크린 리더에는 읽히지 않게 둔다. */}
                                  <img
                                    src={getServerImagePath(subNode)}
                                    alt=""
                                    width={28}
                                    className="shrink-0"
                                  />
                                  <div className="truncate" title={subNode.applicationName}>
                                    {subNode.applicationName}
                                  </div>
                                </ServerMapMenuItem>
                              );
                            })
                          )}
                        </div>
                      </ServerMapMenuContent>
                    );
                  })()}
                {popperContentType === SERVERMAP_MENU_CONTENT_TYPE.SERVICE_GROUP_LINK_LIST &&
                  (() => {
                    const search = serviceGroupSearch.trim().toLowerCase();
                    const subLinks = serviceGroupLinkTargetRef.current?.subLinks ?? [];
                    const filteredSubLinks = search
                      ? subLinks.filter(
                          (l) =>
                            l.sourceInfo?.applicationName?.toLowerCase().includes(search) ||
                            l.targetInfo?.applicationName?.toLowerCase().includes(search),
                        )
                      : subLinks;
                    // 서비스 그룹 링크의 from/to는 ServiceGroupNode key를 가리키므로,
                    // flatten된 nodeDataArray에서 노드를 찾아 applicationName(=serviceName)을 제목에 사용한다.
                    const linkFromKey = serviceGroupLinkTargetRef.current?.from;
                    const linkToKey = serviceGroupLinkTargetRef.current?.to;
                    const nodes =
                      (data?.applicationMapData?.nodeDataArray as
                        GetServerMap.NodeData[] | undefined) ?? [];
                    const fromNode = nodes.find((n) => n.key === linkFromKey);
                    const toNode = nodes.find((n) => n.key === linkToKey);
                    const linkTitle = serviceGroupLinkTargetRef.current
                      ? `${fromNode?.applicationName ?? linkFromKey} → ${toNode?.applicationName ?? linkToKey}`
                      : 'Service Group';
                    return (
                      <ServerMapMenuContent
                        title={linkTitle}
                        onClose={() => setPopperContentType(undefined)}
                        className="w-max min-w-80 max-w-[22.5rem]"
                      >
                        <div className="px-3 pb-2">
                          <div className="relative">
                            <FaSearch className="absolute -translate-y-1/2 pointer-events-none left-2 top-1/2 text-muted-foreground" />
                            <Input
                              ref={serviceGroupSearchRef}
                              placeholder={t('COMMON.SEARCH_INPUT')}
                              value={serviceGroupSearch}
                              onChange={(e) => setServiceGroupSearch(e.target.value)}
                              className="h-8 pl-7"
                            />
                          </div>
                        </div>
                        <div className="overflow-y-auto max-h-72">
                          {filteredSubLinks.length === 0 ? (
                            <div className="px-3 py-2 text-muted-foreground">
                              {t('COMMON.EMPTY_ON_SEARCH')}
                            </div>
                          ) : (
                            filteredSubLinks.map((subLink) => {
                              const isSelected = subLink.key === selectedSubLinkId;
                              const fromName = subLink.sourceInfo?.applicationName ?? subLink.from;
                              const toName = subLink.targetInfo?.applicationName ?? subLink.to;
                              return (
                                <ServerMapMenuItem
                                  key={subLink.key}
                                  className={cn(isSelected && 'bg-accent font-semibold')}
                                  onClick={() => onClickSubLink?.(subLink)}
                                >
                                  <div
                                    className="flex items-center flex-1 min-w-0 gap-1"
                                    title={`${fromName} → ${toName}`}
                                  >
                                    <span className="truncate">{fromName}</span>
                                    <span className="shrink-0 text-muted-foreground">→</span>
                                    <span className="truncate">{toName}</span>
                                  </div>
                                  <div className="ml-2 text-muted-foreground shrink-0">
                                    {addCommas(subLink.totalCount ?? 0)}
                                  </div>
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
                  renderNode={(node, transactionStatusSVGString, isSelected) => {
                    if (node?.subNodesCount !== undefined) {
                      // 서비스 그룹 노드: 이름은 동그라미 하단(라벨)에 두고, 원은 이중선으로 표현한다.
                      // 두 원 모두 SVG로 직접 그려(바깥 테두리는 숨김) 굵기·간격·색을 제어한다.
                      // 가운데를 가르던 가로선은 없애고 노드 개수를 원 중앙에 배치한다.
                      // 기본은 회색(#ddd), 선택 시에는 하이라이트색(#4A61D1)을 사용한다.
                      // 바깥 원(r=48)은 얇게(1.5), 안쪽 원(r=44)은 굵기 3.
                      const ringColor = isSelected ? '#4A61D1' : '#ddd';
                      return `
                  <circle cx="50" cy="50" r="48" fill="none" stroke="${ringColor}" stroke-width="1.5" />
                  <circle cx="50" cy="50" r="44" fill="none" stroke="${ringColor}" stroke-width="3" />
                  <text x="50" y="50" font-size="22" font-weight="bold" dominant-baseline="central" text-anchor="middle" font-family="Arial, Helvetica, sans-serif">${node.subNodesCount}</text>
                `;
                    }
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
                  renderNodeLabel={(node) => {
                    return node?.label ?? '';
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
