import cytoscape from 'cytoscape';
import _ from 'lodash';
import { ServerMapProps } from '../../ui';
import { defaultTheme, GraphStyle, ServerMapTheme } from './theme';

export const getTheme = (theme: ServerMapTheme) => {
  return _.merge({}, defaultTheme, theme);
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
        width: GraphStyle.NODE_WIDTH,
        height: GraphStyle.NODE_HEIGHT,
        label: (el: cytoscape.NodeCollection) => {
          const nodeData = cy.data(el.data()?.id)?.data;
          return nodeLabelRenderer?.(nodeData) || nodeData?.label || '';
        },
        'background-image': (el: cytoscape.NodeCollection) => {
          const nodeData = cy.data(el.data()?.id)?.data;
          return nodeData?.imgArr;
        },
        'background-fit': 'contain' as cytoscape.Css.PropertyValueNode<'contain'>,
        'background-offset-y': '-5px',
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
