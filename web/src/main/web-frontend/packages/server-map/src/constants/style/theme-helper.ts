import _ from 'lodash';
import { MergedEdge, MergedNode } from '../../types';
import { defaultTheme } from './theme';

export const getTheme = (theme: any) => {
  return _.merge(defaultTheme, theme);
}

export const getServerMapStyle = ({
  theme,
  edgeLabelRenderer,
  nodeLabelRenderer,
}: {
  theme: any,
  edgeLabelRenderer?: (edge: MergedEdge) => string;
  nodeLabelRenderer?: (node: MergedNode) => string;
}) => {
  return [
    {
      selector: 'node',
      style: {
        ...theme.node.default,
        'width': 100,
        'height': 100,
        'label': (el: cytoscape.NodeCollection) => nodeLabelRenderer?.(el.data()) || el.data('label'),
        'background-fit': 'contain',
        'background-offset-y': '-5px',
        'background-image': (ele: cytoscape.NodeCollection) => ele.data('imgArr'),
      },
    },
    {
      selector: 'edge',
      style: {
        ...theme.edge.default,
        'label': (el: cytoscape.EdgeCollection) => edgeLabelRenderer?.(el.data()) || '',
      }
    },
    {
      selector: 'edge:loop',
      style: {
        ...theme.edge.loop,
      }
    },
  ]
}