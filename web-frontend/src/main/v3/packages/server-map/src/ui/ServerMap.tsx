import React from 'react';
import cytoscape from 'cytoscape';
import dagre, { DagreLayoutOptions } from 'cytoscape-dagre';
import styled from '@emotion/styled';

import { Node, Edge, MergedNode, MergedEdge } from '../types';
import { getServerMapData } from '../core/merge';
import { getServerMapStyle, getTheme } from '../constants/style/theme-helper';
import { ServerMapTheme } from '../constants/style/theme';

type ClickEventHandler<T> = (param: {
  data?: T,
  eventType: 'right' | 'left',
  position: cytoscape.Position,
}) => void;

export interface ServerMapProps {
  data: {
    nodes: Node[],
    edges: Edge[],
  };
  baseNodeId: string;
  customTheme?: ServerMapTheme;
  onClickNode?: ClickEventHandler<MergedNode>;
  onClickEdge?: ClickEventHandler<MergedEdge>;
  onClickBackground?: ClickEventHandler<{}>;
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
  renderNodeLabel,
  renderEdgeLabel,
}: ServerMapProps) => {
  const container = React.useRef<HTMLDivElement>(null);
  const graph = React.useRef<cytoscape.Core>();
  const layout = React.useRef<cytoscape.Layouts>();

  const processedData = getServerMapData(data);
  const serverMapTheme = getTheme(customTheme);
  const serverMapStyle = getServerMapStyle({
    theme: serverMapTheme,
    edgeLabelRenderer: renderEdgeLabel,
    nodeLabelRenderer: renderNodeLabel,
  });

  React.useEffect(() => {
    if (!container.current) {
      return;
    }
    try {
      if (!graph.current) {
        cytoscape.use(dagre);
        graph.current = cytoscape({
          zoom: 1,
          minZoom: 0.1,
          maxZoom: 3,
          wheelSensitivity: 0.2,
          container: container.current,
          style: [
            ...serverMapStyle,
          ]
        });
      }
    } catch (error) {
      console.error(error);
    }
    return () => {
      graph.current && graph.current.destroy();
    };
  }, []);

  React.useEffect(() => {
    if (graph.current && data?.nodes?.length > 0) {
      if (layout.current) {
        layout.current.stop();
        graph.current.removeAllListeners();
        graph.current.elements().remove();
      }
      graph.current.add(processedData);
      addEventListener();

      layout.current = graph.current.elements().makeLayout({
        name: 'dagre',
        fit: false,
        rankDir: 'LR',
        rankSep: 200,
      } as DagreLayoutOptions);
      layout.current.run();
    }
  }, [data]);

  const handleClickNode = (param: Parameters<ClickEventHandler<MergedNode>>[0]) => {
    onClickNode?.(param);
  }

  const handleClickLink = (param: Parameters<ClickEventHandler<MergedEdge>>[0]) => {
    onClickEdge?.(param);
  }

  const handleClickBackground = (param: Parameters<ClickEventHandler<any>>[0]) => {
    onClickBackground?.(param);
  }

  const addEventListener = () => {
    const cy = graph?.current;
    if (cy) {
      const mainNode = cy.getElementById(baseNodeId);
      cy
        .on('layoutready', () => {
          mainNode.style(serverMapTheme.node?.main!);
          mainNode.style(serverMapTheme.node?.highlight!);
          mainNode.connectedEdges().style(serverMapTheme.edge?.highlight!);
          cy.center(mainNode)
        })
        .on('tap', ({ target, originalEvent }) => {
          const eventType = 'left';
          const position = {
            x: originalEvent.clientX,
            y: originalEvent.clientY
          };

          if (target === cy) {
            handleClickBackground({
              eventType,
              position,
            })
          } else if (target.isNode()) {
            cy.nodes().style(serverMapTheme.node?.default!);
            cy.edges().style(serverMapTheme.edge?.default!);
            cy.getElementById(baseNodeId).style(serverMapTheme.node?.main!);
            target.style(serverMapTheme.node?.highlight);
            target.connectedEdges().style(serverMapTheme.edge?.highlight);

            handleClickNode({
              eventType,
              position,
              data: target.data(),
            })
          } else if (target.isEdge()) {
            cy.nodes().style(serverMapTheme.node?.default!);
            cy.edges().style(serverMapTheme.edge?.default!);
            cy.getElementById(baseNodeId).style(serverMapTheme.node?.main!);
            target.connectedNodes().style({ 'border-color': serverMapTheme.node?.highlight?.['border-color']! });
            target.style(serverMapTheme.edge?.highlight);

            handleClickLink({
              eventType,
              position,
              data: target.data(),
            });
          }
        })
        .on('cxttap', ({ target, originalEvent }) => {
          const eventType = 'right';
          const position = {
            x: originalEvent.clientX,
            y: originalEvent.clientY
          };

          if (target === cy) {
            handleClickBackground({
              eventType,
              position,
            })
          } else if (target.isNode()) {
            handleClickNode({
              eventType,
              position,
              data: target.data(),
            })
          } else if (target.isEdge()) {
            handleClickLink({
              eventType,
              position,
              data: target.data(),
            });
          }
        })
    }
  }

  return (
    <StyledContainer>
      <StyledServerMapWrapper ref={container} />
    </StyledContainer>
  );
};

const StyledContainer = styled.div`
  width: 100%;
  height: 100%;
`;

const StyledServerMapWrapper = styled.div`
  overflow: hidden;
  width: 100%;
  height: 100%;
`

