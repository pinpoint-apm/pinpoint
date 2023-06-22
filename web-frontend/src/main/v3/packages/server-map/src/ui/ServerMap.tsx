import React from 'react';
import cytoscape, { InputEventObject } from 'cytoscape';
import dagre, { DagreLayoutOptions } from 'cytoscape-dagre';

import { Node, Edge, MergedNode, MergedEdge, MergeInfo } from '../types';
import { getMergedData } from '../core/merge';
import { getServerMapStyle, getTheme } from '../constants/style/theme-helper';
import { ServerMapTheme } from '../constants/style/theme';
import { keyBy } from 'lodash';

cytoscape.use(dagre);

type ClickEventHandler<T> = (param: {
  data?: T;
  eventType: 'right' | 'left';
  position: cytoscape.Position;
}) => void;

export interface ServerMapProps
  extends Pick<React.HTMLProps<HTMLDivElement>, 'className' | 'style'> {
  data: {
    nodes: Node[];
    edges: Edge[];
  };
  baseNodeId: string;
  customTheme?: ServerMapTheme;
  onClickNode?: ClickEventHandler<MergedNode>;
  onClickEdge?: ClickEventHandler<MergedEdge>;
  onClickBackground?: ClickEventHandler<{}>;
  onDataMerged?: (mergeInfo: MergeInfo) => void;
  renderNodeLabel?: (node: MergedNode) => string | undefined;
  renderEdgeLabel?: (edge: MergedEdge) => string | undefined;
}

export const ServerMap = ({
  data,
  customTheme = {},
  baseNodeId,
  onClickNode,
  onClickEdge,
  onClickBackground,
  onDataMerged,
  renderNodeLabel,
  renderEdgeLabel,
  className,
  style,
}: ServerMapProps) => {
  const containerRef = React.useRef<HTMLDivElement>(null);
  const baseNodeIdRef = React.useRef<string>();
  const cyRef = React.useRef<cytoscape.Core>();
  const layoutRef = React.useRef<cytoscape.Layouts>();
  const serverMapTheme = getTheme(customTheme);

  React.useEffect(() => {
    return () => {
      cyRef?.current?.destroy();
    };
  }, []);

  React.useEffect(() => {
    baseNodeIdRef.current = baseNodeId;

    if (cyRef.current) {
      layoutRef.current?.removeAllListeners();
      layoutRef.current?.stop();
      layoutRef.current = undefined;
      cyRef.current.removeData();
      cyRef.current.removeAllListeners();
      cyRef.current.destroy();
      cyRef.current = undefined;
    }

    cyRef.current = cytoscape({
      zoom: 1,
      minZoom: 0.1,
      maxZoom: 3,
      wheelSensitivity: 0.2,
      container: containerRef.current,
    });
    cyRef.current.style(
      getServerMapStyle({
        cy: cyRef.current,
        theme: serverMapTheme,
        edgeLabelRenderer: renderEdgeLabel,
        nodeLabelRenderer: renderNodeLabel,
      })
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
        })
      );
    }
  }, [renderNodeLabel, renderEdgeLabel]);

  React.useEffect(() => {
    if (data) {
      const cy = cyRef.current;
      if (cy) {
        const { serverMapData: newData, mergeInfo } = getMergedData(data);
        onDataMerged?.(mergeInfo);

        cy.batch(() => {
          cy.removeData();
          cy.data(keyBy(newData, 'data.id'));

          const currentNodes = cy.nodes();
          const currentEdges = cy.edges();

          currentNodes.forEach((node) => {
            const nodeData = node.data();

            if (!cy.data()[nodeData.id]) {
              node.remove();
            }
          });

          currentEdges.forEach((edge) => {
            const edgeData = edge.data();

            if (!cy.data()[edgeData.id]) {
              edge.remove();
            }
          });

          const removedNodes = cy.nodes();
          const removedEdges = cy.edges();

          newData.forEach(({ data }) => {
            if ((data as Edge).source) {
              // edge
              if (!removedEdges.some((edges) => edges.data().id === data.id)) {
                cy.add({ data });
              }
            } else {
              //node
              if (!removedNodes.some((node) => node.data().id === data.id)) {
                // newNodeIds.push(data.id);
                cy.add({ data });
              }
            }
          });
        });

        if (!layoutRef.current) {
          layoutRef.current = cy?.layout({
            name: 'dagre',
            fit: false,
            rankDir: 'LR',
            rankSep: 200,
          } as DagreLayoutOptions);
          layoutRef.current.run();
        }
      }
    }
  }, [data]);

  const handleClickNode = (param: Parameters<ClickEventHandler<MergedNode>>[0]) => {
    onClickNode?.(param);
  };

  const handleClickLink = (param: Parameters<ClickEventHandler<MergedEdge>>[0]) => {
    onClickEdge?.(param);
  };

  const handleClickBackground = (param: Parameters<ClickEventHandler<any>>[0]) => {
    onClickBackground?.(param);
  };

  const addEventListener = React.useCallback(() => {
    const cy = cyRef?.current;

    if (cy) {
      cy.on('layoutready', () => {
        highlightNode(cy.getElementById(baseNodeId));
        const mainNode = cy.getElementById(baseNodeId);
        cy.resize();
        cy.center(mainNode);
      })
        .on('mouseover', ({ target }) => {
          cy.container()!.style.cursor = target === cy ? 'default' : 'pointer';
        })
        .on('mouseout', () => {
          cy.container()!.style.cursor = 'default';
        })
        .on('tap', ({ target, originalEvent, renderedPosition }: InputEventObject) => {
          const eventType = 'left';
          const position = {
            x: renderedPosition.x,
            y: renderedPosition.y,
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
          } else if (target.isEdge()) {
            hightlightEdge(target);

            handleClickLink({
              eventType,
              position,
              data: target.data(),
            });
          }
        })
        .on('cxttap', ({ target, renderedPosition }: InputEventObject) => {
          const eventType = 'right';
          const position = {
            x: renderedPosition.x,
            y: renderedPosition.y,
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
  }, [onClickNode, onClickEdge, onClickBackground]);

  const highlightNode = (target: cytoscape.CollectionReturnValue) => {
    const cy = cyRef.current!;
    cy.nodes().style(serverMapTheme.node?.default!);
    cy.edges().style(serverMapTheme.edge?.default!);
    cy.getElementById(baseNodeId).style(serverMapTheme.node?.main!);
    target.style(serverMapTheme.node?.highlight!);
    target.connectedEdges().style(serverMapTheme.edge?.highlight!);
  };

  const hightlightEdge = (target: cytoscape.CollectionReturnValue) => {
    const cy = cyRef.current!;

    cy.nodes().style(serverMapTheme.node?.default!);
    cy.edges().style(serverMapTheme.edge?.default!);
    cy.getElementById(baseNodeId).style(serverMapTheme.node?.main!);
    target
      .connectedNodes()
      .style({ 'border-color': serverMapTheme.node?.highlight?.['border-color']! });
    target.style(serverMapTheme.edge?.highlight!);
  };

  return (
    <div
      style={{ width: '100%', height: '100%', overflow: 'hidden', ...style }}
      className={className}
      ref={containerRef}
    />
  );
};
