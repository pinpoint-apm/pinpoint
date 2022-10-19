import React from 'react';
import cytoscape from 'cytoscape';
import dagre, { DagreLayoutOptions } from 'cytoscape-dagre';
import styled from '@emotion/styled';

import { Node, Edge, MergedNode, MergedEdge } from '../types';
import { getServerMapData } from '../core/merge';
import { getServerMapStyle, getTheme } from '../constants/style/theme-helper';

type ClickEventHandler<T> = (param: {
  data?: T,
  eventType: 'right' | 'left',
  position: cytoscape.Position,
}) => void;

export interface ServerMapProps {
  baseNodeKey?: string;
  data: {
    nodes: Node[],
    edges: Edge[],
  };
  customTheme?: any;
  onClickNode?: ClickEventHandler<MergedNode>;
  onClickLink?: ClickEventHandler<MergedEdge>;
  onClickBackground?: ClickEventHandler<{}>;
  renderNodeLabel?: (node: MergedNode) => string;
  renderEdgeLabel?: (edge: MergedEdge) => string;
  renderNoData?: () => React.ReactNode;
}

export const ServerMap = ({
  data,
  customTheme,
  baseNodeKey = '',
  onClickNode,
  onClickLink,
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

  const handleClickNode = (param: Parameters<ClickEventHandler<MergedNode>>[0]) => {
    onClickNode?.(param);
  }

  const handleClickLink = (param: Parameters<ClickEventHandler<MergedEdge>>[0]) => {
    onClickLink?.(param);
  }

  const handleClickBackground = (param: Parameters<ClickEventHandler<any>>[0]) => {
    onClickBackground?.(param);
  }

  const addEventListener = () => {
    const cy = graph?.current;
    if (cy) {
      cy
        .on('layoutready', () => {
          const mainNode = cy.getElementById(baseNodeKey);
          cy.zoom(1);
          cy.center(mainNode);
          mainNode.style(serverMapTheme.node.main);
          mainNode.style(serverMapTheme.node.highlight);
          mainNode.connectedEdges().style(serverMapTheme.edge.highlight);
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
            cy.nodes().style(serverMapTheme.node.default);
            cy.edges().style(serverMapTheme.edge.default);
            cy.getElementById(baseNodeKey).style(serverMapTheme.node.main);
            target.style(serverMapTheme.node.highlight);
            target.connectedEdges().style(serverMapTheme.edge.highlight);

            handleClickNode({
              eventType,
              position,
              data: target.data(),
            })
          } else if (target.isEdge()) {
            cy.nodes().style(serverMapTheme.node.default);
            cy.edges().style(serverMapTheme.edge.default);
            cy.getElementById(baseNodeKey).style(serverMapTheme.node.main);
            target.connectedNodes().style({ 'border-color': serverMapTheme.node.highlight['border-color'] });
            target.style(serverMapTheme.edge.highlight);

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

  return (
    <StyledContainer>
      <StyledServerMapWrapper ref={container} />
    </StyledContainer>
  );
};

const StyledContainer = styled.div`
  position: relative;
  width: 100%;
  height: 100%; 
  overflow: hidden;
`;

const StyledServerMapWrapper = styled.div`
  overflow: hidden;
  width: 100%;
  height: 100%;
`

