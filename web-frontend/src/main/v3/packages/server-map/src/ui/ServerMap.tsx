import React from 'react';
import cytoscape, { InputEventObject } from 'cytoscape';
import dagre, { DagreLayoutOptions } from 'cytoscape-dagre';

import { Node, Edge, MergedNode, MergedEdge, MergeInfo } from '../types';
import { getMergedData } from '../core/merge';
import { placeAnchoredNodes, ShiftRecord, snapshotNodes } from '../core/anchor';
import { getServerMapStyle, getTheme } from '../constants/style/theme-helper';
import { GraphStyle, ServerMapTheme } from '../constants/style/theme';
import { keyBy } from 'lodash';

cytoscape.use(dagre);

type ClickEventHandler<T> = (param: {
  data?: T;
  eventType: 'right' | 'left' | 'programmatic' | 'hover';
  position: Partial<cytoscape.Position>;
  target?: cytoscape.CollectionReturnValue;
  isLeftNode?: boolean;
}) => void;

export interface ServerMapProps extends Pick<
  React.HTMLProps<HTMLDivElement>,
  'className' | 'style'
> {
  data: {
    nodes: Node[];
    edges: Edge[];
  };
  baseNodeId: string;
  customTheme?: ServerMapTheme;
  forceLayoutUpdate?: boolean;
  onHoverNode?: ClickEventHandler<MergedNode>;
  onClickNode?: ClickEventHandler<MergedNode>;
  onDoubleClickNode?: ClickEventHandler<MergedNode>;
  /**
   * 노드를 한 번만 클릭했을 때. `onClickNode`와 달리 더블클릭의 일부인 클릭에서는 불리지 않는다.
   * 두 번째 클릭을 기다리느라 `onClickNode`보다 조금(cytoscape `multiClickDebounceTime`) 늦다.
   */
  onSingleClickNode?: ClickEventHandler<MergedNode>;
  /**
   * 바깥에서 정한 선택(노드 또는 링크의 id). 바뀌면 그 요소를 하이라이트한다.
   *
   * 그래프를 클릭하지 않고 고른 경우(묶음 노드의 자식 목록 팝업 등)에도 선택을 기억해 두기 위한
   * 것이다. 그 요소가 그래프에 없으면 그것을 대신 그리고 있는 요소(`memberIds`)를 하이라이트한다.
   */
  selectedId?: string;
  onClickEdge?: ClickEventHandler<MergedEdge>;
  onClickBackground?: ClickEventHandler<object>;
  onDataMerged?: (mergeInfo: MergeInfo) => void;
  renderNodeLabel?: (node: MergedNode) => string | undefined;
  renderEdgeLabel?: (edge: MergedEdge) => string | undefined;
  renderNode?: (
    node: MergedNode,
    transactionStatusSVGString: string,
    isSelected?: boolean,
  ) => string;
  cy?: (cy: cytoscape.Core) => void;
}

export const ServerMap = ({
  data,
  customTheme = {},
  baseNodeId,
  forceLayoutUpdate,
  onHoverNode,
  onClickNode,
  onDoubleClickNode,
  onSingleClickNode,
  selectedId,
  onClickEdge,
  onClickBackground,
  onDataMerged,
  renderNodeLabel,
  renderEdgeLabel,
  renderNode,
  className,
  style,
  cy,
}: ServerMapProps) => {
  const containerRef = React.useRef<HTMLDivElement>(null);
  const cyRef = React.useRef<cytoscape.Core | undefined>(undefined);
  const layoutRef = React.useRef<cytoscape.Layouts | undefined>(undefined);
  const serverMapTheme = getTheme(customTheme);
  const [selectedElementId, setSelectedElementId] = React.useState('');
  // 펼칠 때 주변 노드를 비켜 세운 양(기준 노드 id → 이동량). 다시 묶을 때 되돌린다.
  const shiftsRef = React.useRef(new Map<string, ShiftRecord>());

  React.useEffect(() => {
    return () => {
      cyRef.current?.destroy();
    };
  }, []);

  React.useEffect(() => {
    setSelectedElementId(baseNodeId);

    if (cyRef.current) {
      layoutRef.current?.removeAllListeners();
      layoutRef.current?.stop();
      layoutRef.current = undefined;
      cyRef.current?.removeData();
      cyRef.current?.removeAllListeners();
      cyRef.current?.destroy();
      cyRef.current = undefined;
    }

    cyRef.current = cytoscape({
      zoom: 1,
      minZoom: 0.1,
      maxZoom: 3,
      wheelSensitivity: 0.2,
      container: containerRef.current,
    });
    cy?.(cyRef.current);

    cyRef.current.style(
      getServerMapStyle({
        cy: cyRef.current,
        theme: serverMapTheme,
        edgeLabelRenderer: renderEdgeLabel,
        nodeLabelRenderer: renderNodeLabel,
      }),
    );

    addEventListener();
  }, [baseNodeId]);

  React.useEffect(() => {
    const cy = cyRef.current;
    if (cy) {
      cy.style(
        getServerMapStyle({
          cy,
          theme: serverMapTheme,
          edgeLabelRenderer: renderEdgeLabel,
          nodeLabelRenderer: renderNodeLabel,
        }),
      );
    }
  }, [renderNodeLabel, renderEdgeLabel]);

  React.useEffect(() => {
    if (data) {
      const cy = cyRef.current;
      if (cy) {
        const { nodes: newNodes, edges: newEdges, mergeInfo } = getMergedData(data, renderNode);
        let addedNodes: cytoscape.CollectionReturnValue[] | undefined;
        onDataMerged?.(mergeInfo);
        // 지워지기 전 위치. 새 노드를 anchorId 노드가 있던 자리에 놓는 데 쓴다.
        const prevSnapshot = layoutRef.current ? snapshotNodes(cy) : undefined;

        cy.batch(() => {
          cy.removeData();
          cy.data(keyBy([...newNodes, ...newEdges], 'data.id'));

          const oldNodeKeys = cy.nodes().map((node) => node.id());
          // 부모(compound) 노드를 자식보다 먼저 추가해야 자식의 parent가 걸린다.
          const parentIds = new Set(newNodes.map(({ data }) => data.parent).filter(Boolean));
          const newNodeKeys = newNodes
            .map(({ data }) => data.id)
            .sort((a, b) => Number(parentIds.has(b)) - Number(parentIds.has(a)));

          new Set([...oldNodeKeys, ...newNodeKeys]).forEach((key) => {
            const isOldNodes = oldNodeKeys.includes(key);
            const isNewNodes = newNodeKeys.includes(key);
            const shouldRemove = isOldNodes && !isNewNodes;
            const shouldAdd = !isOldNodes && isNewNodes;

            if (shouldRemove) {
              const node = cy.getElementById(key);

              node.remove();
              node.connectedEdges().remove();
            } else if (shouldAdd) {
              const { data } = newNodes.find(({ data }) => data.id === key)!;
              const connectedEdges = newEdges.filter(
                ({ data }) => data.source === key || data.target === key,
              );

              addedNodes = addedNodes ? [...addedNodes, cy.add({ data })] : [cy.add({ data })]; // add node
              connectedEdges.forEach(({ data }) => {
                const sourceNode = cy.getElementById(data.source);
                const targetNode = cy.getElementById(data.target);

                if (sourceNode.inside() && targetNode.inside() && cy) {
                  cy.add({ data }); // add edge
                }
              });
            } else {
              return;
            }
          });
        });

        const added = addedNodes ?? [];
        const isAnchored = (node: cytoscape.CollectionReturnValue) => {
          const anchorId = node.data('anchorId');
          return Boolean(anchorId && prevSnapshot?.has(anchorId));
        };
        const anchored = prevSnapshot ? added.filter(isAnchored) : [];
        const unanchored = prevSnapshot ? added.filter((node) => !isAnchored(node)) : added;
        // 펼치기/묶기처럼 추가된 노드가 모두 제자리가 정해진 경우에는 전체 배치를 다시 돌리지 않는다.
        // 다시 돌리면 map 전체가 새로 배치되어 보던 자리를 잃는다.
        const isAnchoredChange = anchored.length > 0 && unanchored.length === 0;

        if (!layoutRef.current || (forceLayoutUpdate && !isAnchoredChange)) {
          layoutRef.current = cy?.layout({
            name: 'dagre',
            fit: false,
            rankDir: 'LR',
            rankSep: 200,
          } as DagreLayoutOptions);
          layoutRef.current?.run();
        } else {
          if (anchored.length > 0) {
            placeAnchoredNodes(cy, anchored, prevSnapshot!, shiftsRef.current);
          }
          const centerNode = cy.getElementById(baseNodeId);
          // 기준 노드가 없는 map(비DEFAULT servicemap)에서는 빈 컬렉션이라 위치를 읽을 수 없다
          // (position()이 undefined). 펼치거나 접으면서 병합 노드의 id가 바뀌면 anchor 없는 노드가
          // 생기므로, 실시간 보기에서 더블클릭만으로 이 경로에 들어올 수 있다.
          if (unanchored.length > 0 && centerNode.nonempty()) {
            const { x: centerNodeX, y: centerNodeY } = centerNode.position();
            const { y1: _centerNodeY1, y2: centerNodeY2 } = centerNode.boundingBox();
            let rankDiff: number; // Indicates rank diff between added node and the center node

            unanchored.forEach((addedNode: any) => {
              rankDiff = 0;
              const predecessors = addedNode.predecessors();
              const successors = addedNode.successors();

              const hasIncomers = predecessors.contains(centerNode); // or hasOutgoers
              const traverseTarget = hasIncomers ? predecessors : successors;

              rankDiff =
                traverseTarget
                  .nodes()
                  .toArray()
                  .findIndex((ele: any) => ele.id() === baseNodeId) + 1;
              const newX =
                centerNodeX +
                rankDiff * (GraphStyle.RANK_SEP + GraphStyle.NODE_WIDTH) * (hasIncomers ? 1 : -1);

              const { y } = addedNode.position();
              const { h, y1 } = addedNode.boundingBox();
              const labelHeight = h - (y - y1) * 2;

              const overlayableNodes = cy.nodes().filter((node: any) => {
                const isSameNode = node.same(addedNode);
                const { x } = node.position();
                const width = node.width();
                const isXPosOverlaid = Math.abs(newX - x) <= width;

                return !isSameNode && isXPosOverlaid;
              });

              let newY1;

              if (Math.random() >= 0.5) {
                // Add at the top
                const topY = Math.min(
                  ...overlayableNodes.map(
                    (node: any) => node.position().y - GraphStyle.NODE_RADIUS,
                  ),
                  centerNodeY - GraphStyle.NODE_RADIUS,
                );
                const newY2 = topY - GraphStyle.NODE_RADIUS;

                newY1 = newY2 - h;
              } else {
                // Add at the bottom
                const bottomY = Math.max(
                  ...overlayableNodes.map((node: any) => node.boundingBox().y2),
                  centerNodeY2,
                );

                newY1 = bottomY + GraphStyle.NODE_RADIUS;
              }

              const newY = (h - labelHeight) / 2 + newY1;

              addedNode.position({
                x: newX,
                y: newY,
              });
            });
          }

          if (added.length > 0) {
            // 묶여서 선택된 요소가 사라졌으면 그것을 대신 그리는 묶음 요소를 하이라이트한다.
            // 선택은 그대로 두어, 다시 펼치면 원래 요소가 하이라이트된다.
            //
            // 바깥에서 정한 선택(selectedId)을 먼저 본다. 마지막으로 클릭한 요소(selectedElementId)는
            // 선택이 아닐 수 있다 — 묶음 노드나 상자를 더블클릭하면 그 첫 클릭이 클릭한 요소가 되는데,
            // 그것들은 조회 대상이 아니고 펼치거나 접으면 곧바로 사라진다.
            const fromSelectedId = selectedId ? findDrawnElement(cy, selectedId) : undefined;
            const selectedElement = fromSelectedId?.nonempty()
              ? fromSelectedId
              : findDrawnElement(cy, selectedElementId);

            if (selectedElement.nonempty()) {
              highlightElement(selectedElement);
            } else {
              highlightNode(cy?.getElementById(baseNodeId));
            }
          }
        }
      }
    }
  }, [data]);

  React.useEffect(() => {
    const cy = cyRef.current;
    if (!cy || !selectedId) {
      return;
    }
    const selectedElement = findDrawnElement(cy, selectedId);
    // 아직 그려지지 않은 요소(데이터 도착 전)라면 지금 선택을 건드리지 않는다.
    if (selectedElement.nonempty()) {
      setSelectedElementId(selectedId);
      highlightElement(selectedElement);
    }
  }, [selectedId]);

  const handleClickNode = (param: Parameters<ClickEventHandler<MergedNode>>[0]) => {
    onClickNode?.(param);
  };

  const handleClickLink = (param: Parameters<ClickEventHandler<MergedEdge>>[0]) => {
    onClickEdge?.(param);
  };

  const handleClickBackground = (param: Parameters<ClickEventHandler<any>>[0]) => {
    onClickBackground?.(param);
  };

  const handleHoverNode = (param: Parameters<ClickEventHandler<MergedNode>>[0]) => {
    onHoverNode?.(param);
  };

  const handleDoubleClickNode = (param: Parameters<ClickEventHandler<MergedNode>>[0]) => {
    onDoubleClickNode?.(param);
  };

  const handleSingleClickNode = (param: Parameters<ClickEventHandler<MergedNode>>[0]) => {
    onSingleClickNode?.(param);
  };

  const addEventListener = React.useCallback(() => {
    const cy = cyRef.current;

    if (cy) {
      // 전체 배치를 새로 하면 펼칠 때 비켜 세운 기록은 더 이상 지금 위치와 맞지 않는다.
      cy.on('layoutstart', () => {
        shiftsRef.current.clear();
      });
      cy.on('layoutready', () => {
        const baseNode = cy.getElementById(baseNodeId);
        highlightNode(baseNode);
        cy.resize();
        // baseNodeId가 실제 노드 id와 어긋나면 baseNode가 빈 컬렉션이고,
        // cy.center(empty)는 아무 동작도 하지 않아 노드가 화면 밖/구석에 남는다.
        // 이 경우 전체 노드 기준으로 센터링해 항상 화면 안에 보이도록 한다.
        // (엣지를 제외한 cy.nodes()를 사용해 노드 클러스터 중심에 더 가깝게 맞춘다.)
        cy.center(baseNode.nonempty() ? baseNode : cy.nodes());
      })
        .on('mouseover', ({ target, renderedPosition }) => {
          cy.container()!.style.cursor = target === cy ? 'default' : 'pointer';

          const position = {
            x: renderedPosition?.x,
            y: renderedPosition?.y,
          };

          if (target?.isNode?.()) {
            const baseNode = cy.getElementById(baseNodeId);
            const isLeftNode = target.renderedPosition()?.x <= baseNode.renderedPosition()?.x;

            handleHoverNode({
              eventType: 'hover',
              position,
              data: target.data(),
              target: target,
              isLeftNode,
            });
          }
        })
        .on('mouseout', ({ target }) => {
          if (target?.isNode?.()) {
            handleHoverNode({
              eventType: 'hover',
              position: {
                x: 0,
                y: 0,
              },
              data: undefined,
            });
          }
          cy.container()!.style.cursor = 'default';
        })
        .on(
          'tap',
          ({ target, originalEvent: _originalEvent, renderedPosition }: InputEventObject) => {
            const eventType = renderedPosition ? 'left' : 'programmatic';
            const position = {
              x: renderedPosition?.x,
              y: renderedPosition?.y,
            };

            if (target === cy) {
              handleClickBackground({
                eventType,
                position,
              });
            } else if (target.isNode()) {
              highlightNode(target);

              handleClickNode({
                eventType,
                position,
                data: target.data(),
              });

              setSelectedElementId(target.id());
            } else if (target.isEdge()) {
              highlightEdge(target);

              handleClickLink({
                eventType,
                position,
                data: target.data(),
              });

              setSelectedElementId(target.id());
            }
          },
        )
        // onetap은 두 번째 클릭이 오지 않았을 때만 온다(더블클릭이면 오지 않는다).
        // 코드로 emit('tap')한 클릭은 사용자 입력이 아니라 여기에 오지 않는다.
        .on('onetap', ({ target, renderedPosition }: InputEventObject) => {
          if (target !== cy && target.isNode() && renderedPosition) {
            handleSingleClickNode({
              eventType: 'left',
              position: {
                x: renderedPosition.x,
                y: renderedPosition.y,
              },
              data: target.data(),
              target,
            });
          }
        })
        .on('dbltap', ({ target, renderedPosition }: InputEventObject) => {
          if (target !== cy && target.isNode()) {
            handleDoubleClickNode({
              eventType: 'left',
              position: {
                x: renderedPosition?.x,
                y: renderedPosition?.y,
              },
              data: target.data(),
              target,
            });
          }
        })
        .on('cxttap', ({ target, renderedPosition }: InputEventObject) => {
          const eventType = 'right';
          const position = {
            x: renderedPosition?.x,
            y: renderedPosition?.y,
          };

          if (target === cy) {
            handleClickBackground({
              eventType,
              position,
            });
          } else if (target.isNode()) {
            handleClickNode({
              eventType,
              position,
              data: target.data(),
            });
          } else if (target.isEdge()) {
            handleClickLink({
              eventType,
              position,
              data: target.data(),
            });
          }
        });
    }
  }, [onClickNode, onDoubleClickNode, onSingleClickNode, onClickEdge, onClickBackground]);

  // 서비스 그룹 노드의 배경 이미지를 기본(imgArr)으로 되돌린다.
  // 인라인 background-image는 서비스 그룹 노드에만 지정한다. 일반 노드는 스타일시트가
  // background-image(cy.data 기반)를 관리하도록 두어야, 데이터 갱신 시 상태 링이 최신으로 유지된다.
  const resetNodeImages = () => {
    const cy = cyRef.current!;
    cy.nodes().forEach((node) => {
      const nodeData = cy.data(node.id())?.data;
      if (nodeData?.subNodesCount !== undefined && nodeData?.imgArr) {
        node.style('background-image', nodeData.imgArr);
      }
    });
  };

  // 대상 노드를 하이라이트 이미지(imgArrHighlight)로 전환한다.
  // imgArrHighlight는 서비스 그룹 노드에만 존재하므로, 일반 노드는 자연히 건드리지 않는다.
  const highlightNodeImage = (target: cytoscape.NodeCollection) => {
    const cy = cyRef.current!;
    target.forEach((node) => {
      const nodeData = cy.data(node.id())?.data;
      if (nodeData?.imgArrHighlight) {
        node.style('background-image', nodeData.imgArrHighlight);
      }
    });
  };

  // 서비스 그룹 노드는 두 원을 SVG로 그리므로 cytoscape 테두리를 숨긴다.
  // 하이라이트 로직이 모든 노드 테두리를 기본값(굵기 3)으로 되돌리므로, 이후 서비스 노드만 다시 0으로 맞춘다.
  const hideServiceNodeBorder = () => {
    const cy = cyRef.current!;
    cy.nodes().forEach((node) => {
      const nodeData = cy.data(node.id())?.data;
      if (nodeData?.subNodesCount !== undefined) {
        node.style('border-width', 0);
      }
    });
  };

  // 노드 스타일을 기본값으로 되돌린다. 부모(compound) 노드는 기본 노드 스타일을 덮어씌우면
  // 상자 모양이 흰 배경·굵은 테두리로 바뀌므로, 인라인 스타일을 지워 스타일시트(node:parent)로 돌린다.
  const resetNodeStyles = () => {
    const cy = cyRef.current!;
    /* eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain */
    cy.nodes(':childless').style(serverMapTheme.node?.default!);
    cy.nodes(':parent').removeStyle();
  };

  /**
   * id의 요소가 그려져 있으면 그것을, 없으면 그것을 대신 그리고 있는 요소(`memberIds`에 그 id가
   * 있는 요소)를 찾는다. 둘 다 없으면 빈 컬렉션이다.
   */
  const findDrawnElement = (cy: cytoscape.Core, id: string) => {
    const element = cy.getElementById(id);
    if (!id || element.nonempty()) {
      return element;
    }
    return cy
      .elements()
      .filter((el) => Boolean(cy.data(el.id())?.data?.memberIds?.includes(id)))
      .first() as cytoscape.CollectionReturnValue;
  };

  const highlightElement = (target: cytoscape.CollectionReturnValue) => {
    if (target.isNode()) {
      highlightNode(target);
    } else {
      highlightEdge(target);
    }
  };

  const highlightNode = (target: cytoscape.CollectionReturnValue) => {
    const cy = cyRef.current!;
    /* eslint-disable @typescript-eslint/no-non-null-asserted-optional-chain */
    resetNodeStyles();
    cy.edges().style(serverMapTheme.edge?.default!);
    resetNodeImages();
    hideServiceNodeBorder();
    cy.getElementById(baseNodeId).style(serverMapTheme.node?.main!);
    target.style(serverMapTheme.node?.highlight!);
    highlightNodeImage(target.nodes());
    target.connectedEdges().style(serverMapTheme.edge?.highlight!);
    /* eslint-enable @typescript-eslint/no-non-null-asserted-optional-chain */
  };

  const highlightEdge = (target: cytoscape.CollectionReturnValue) => {
    const cy = cyRef.current!;

    /* eslint-disable @typescript-eslint/no-non-null-asserted-optional-chain */
    resetNodeStyles();
    cy.edges().style(serverMapTheme.edge?.default!);
    resetNodeImages();
    hideServiceNodeBorder();
    cy.getElementById(baseNodeId).style(serverMapTheme.node?.main!);
    const connectedNodes = target.connectedNodes();
    connectedNodes.style({ 'border-color': serverMapTheme.node?.highlight?.['border-color']! });
    highlightNodeImage(connectedNodes);
    target.style(serverMapTheme.edge?.highlight!);
    /* eslint-enable @typescript-eslint/no-non-null-asserted-optional-chain */
  };

  return (
    <div
      style={{ width: '100%', height: '100%', overflow: 'hidden', ...style }}
      className={className}
      ref={containerRef}
    />
  );
};
