import cytoscape from 'cytoscape';
import _ from 'lodash';
import { ServerMapProps } from '../../ui';
import { defaultTheme, ServerMapTheme } from './theme';

export const getTheme = (theme: ServerMapTheme) => {
  return _.merge(defaultTheme, theme);
}

export const getServerMapStyle = ({
  theme,
  edgeLabelRenderer,
  nodeLabelRenderer,
}: {
  theme: ServerMapTheme,
  edgeLabelRenderer?: ServerMapProps['renderEdgeLabel'];
  nodeLabelRenderer?: ServerMapProps['renderNodeLabel'];
}) => {
  return [
    {
      selector: 'node',
      style: {
        ...theme.node?.default,
        'width': 100,
        'height': 100,
        'label': (el: cytoscape.NodeCollection) => nodeLabelRenderer?.(el.data()) || el.data('label'),
        'background-image': (ele: cytoscape.NodeCollection) => ele.data('imgArr'),
        'background-fit': 'contain' as cytoscape.Css.PropertyValueNode<'contain'>,
        'background-offset-y': '-5px',
      },
    },
    {
      selector: 'edge',
      style: {
        ...theme.edge?.default,
        'label': (el: cytoscape.EdgeCollection) => edgeLabelRenderer?.(el.data()) || '',
      }
    },
    {
      selector: 'edge:loop',
      style: {
        ...theme.edge?.loop,
      }
    },
  ]
}