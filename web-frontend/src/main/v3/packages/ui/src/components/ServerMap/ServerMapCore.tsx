import React from 'react';
import { useTranslation } from 'react-i18next';
import {
  Node,
  Edge,
  ServerMap as ServerMapComponent,
  ServerMapProps as ServerMapComponentProps,
  MergedEdge,
} from '@pinpoint-fe/server-map';
import { FilteredMap, GetServerMap } from '@pinpoint-fe/constants';
import { addCommas, getServerImagePath } from '@pinpoint-fe/utils';
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
  Separator,
  ServerMapQueryOption,
  ServerMapQueryOptionProps,
  ServerMapSearchList,
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '..';

export interface ServerMapCoreProps extends Omit<ServerMapComponentProps, 'data'> {
  data?: GetServerMap.Response | FilteredMap.Response;
  onClickMenuItem?: (type: SERVERMAP_MENU_FUNCTION_TYPE, data: Node | Edge) => void;
  onMergeStateChange?: () => void;
  inputPlaceHolder?: string;
  queryOption?: ServerMapQueryOptionProps['queryOption'];
  onApplyChangedOption?: ServerMapQueryOptionProps['onApply'];
  disableMenu?: boolean;
}

export const ServerMapCore = ({
  data,
  baseNodeId,
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

    allServiceTypes.current = Array.from(nodeTypes);

    const nodes = nodeDataArray.map((node) => {
      return {
        id: node.key,
        label: node.applicationName,
        type: node.serviceType,
        imgPath: getServerImagePath(node),
        transactionInfo: getTransactionInfo(node),
        shouldNotMerge: () => {
          return (
            node.isWas ||
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

  const getTransactionInfo = (node: GetServerMap.NodeData | FilteredMap.NodeData) => {
    const { isWas, isAuthorized } = node;

    if (isWas && isAuthorized) {
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
                <TooltipTrigger>
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
                <TooltipTrigger>
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
              </div>
            </ServerMapMenu>
          )}
          {serverMapData.nodes.length > 0 && (
            <ServerMapComponent
              baseNodeId={baseNodeId}
              data={serverMapData}
              renderNode={(node, transactionStatusSVGString) => {
                return `
                  ${transactionStatusSVGString}
                  ${
                    node.transactionInfo?.instanceCount &&
                    `<text 
                      x="50" y="80"
                      font-size="smaller"
                      dominant-baseline="middle"
                      text-anchor="middle"
                      font-family="Arial, Helvetica, sans-serif"
                    >${node.transactionInfo?.instanceCount}</text>`
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
              onClickBackground={handleClickBackground}
              onClickNode={handleClickNode}
              onClickEdge={handleClickEdge}
              onDataMerged={({ types }) => setCheckedServiceTypes(types)}
              cy={(cy) => {
                cyRef.current = cy;
              }}
              {...props}
            />
          )}
        </>
      )}
    </div>
  );
};
