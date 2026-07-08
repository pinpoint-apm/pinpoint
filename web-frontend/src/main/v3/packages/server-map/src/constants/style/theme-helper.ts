import cytoscape from 'cytoscape';
import { merge } from 'lodash';
import { ServerMapProps } from '../../ui';
import { defaultTheme, GraphStyle, ServerMapTheme } from './theme';

export const getTheme = (theme: ServerMapTheme) => {
  return merge({}, defaultTheme, theme);
};

export const getServerMapStyle = ({
  cy,
  theme,
  edgeLabelRenderer,
  nodeLabelRenderer,
}: {
  cy: cytoscape.Core;
  theme: ServerMapTheme;
  edgeLabelRenderer?: ServerMapProps['renderEdgeLabel'];
  nodeLabelRenderer?: ServerMapProps['renderNodeLabel'];
}) => {
  return [
    {
      selector: 'node',
      style: {
        ...theme.node?.default,
        width: (el: cytoscape.NodeCollection) => {
          const nodeData = cy.data(el.data()?.id)?.data;
          return nodeData?.subNodesCount !== undefined
            ? GraphStyle.NODE_WIDTH * 1.3
            : GraphStyle.NODE_WIDTH;
        },
        height: (el: cytoscape.NodeCollection) => {
          const nodeData = cy.data(el.data()?.id)?.data;
          return nodeData?.subNodesCount !== undefined
            ? GraphStyle.NODE_HEIGHT * 1.3
            : GraphStyle.NODE_HEIGHT;
        },
        label: (el: cytoscape.NodeCollection) => {
          const nodeData = cy.data(el.data()?.id)?.data;
          const customLabel = nodeLabelRenderer?.(nodeData);
          return customLabel ?? nodeData?.label ?? '';
        },
        'background-image': (el: cytoscape.NodeCollection) => {
          const nodeData = cy.data(el.data()?.id)?.data;
          return nodeData?.imgArr;
        },
        'background-fit': 'contain' as cytoscape.Css.PropertyValueNode<'contain'>,
        // 서비스 그룹 노드는 이중선 원이 테두리와 동심원을 이루도록 세로 오프셋을 두지 않는다.
        'background-offset-y': ((el: cytoscape.NodeCollection) => {
          const nodeData = cy.data(el.data()?.id)?.data;
          return nodeData?.subNodesCount !== undefined ? '0px' : '-5px';
        }) as unknown as cytoscape.Css.PropertyValueNode<string>,
        // 서비스 그룹 노드는 두 원을 SVG로 직접 그리므로 cytoscape 테두리는 숨긴다(굵기 0).
        'border-width': ((el: cytoscape.NodeCollection) => {
          const nodeData = cy.data(el.data()?.id)?.data;
          return nodeData?.subNodesCount !== undefined ? 0 : (theme.node?.default?.['border-width'] ?? 3);
        }) as unknown as cytoscape.Css.PropertyValueNode<number>,
      },
    },
    {
      selector: 'edge',
      style: {
        ...theme.edge?.default,
        label: (el: cytoscape.EdgeCollection) => {
          const edgeData = cy.data(el.data()?.id)?.data;
          return edgeLabelRenderer?.(edgeData) || '';
        },
      },
    },
    {
      selector: 'edge:loop',
      style: {
        ...theme.edge?.loop,
      },
    },
  ];
};
