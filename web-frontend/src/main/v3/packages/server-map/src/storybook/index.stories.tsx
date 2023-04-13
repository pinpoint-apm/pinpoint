import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import { ServerMap } from '../ui/ServerMap';
import { data, getServerMapData } from './mock';

export default {
  title: 'PINPOINT/ServerMap',
  component: ServerMap,
  argTypes: {
    backgroundColor: { control: 'color' },
    onClickBackground: { action: 'clicked' },
    onClickNode: { action: 'clicked' },
    onClickEdge: { action: 'clicked' },
  },
} as ComponentMeta<typeof ServerMap>;

const edgeDataById = data.applicationMapData.linkDataArray.reduce((acc, curr) => {
  return {
    ...acc,
    [curr.key]: curr
  }
}, {});
const serverMapData = getServerMapData();

const Template: ComponentStory<typeof ServerMap> = (args) => (
  <div style={{width: '100%', height: '90vh', border: '1px solid black'}}>
    <ServerMap
      {...args}
    />
  </div>
);

export const Default = Template.bind({});
Default.args = {
  baseNodeId: 'ACL-PORTAL-DEV^SPRING_BOOT',
  data: serverMapData,
};

export const RenderCustomLabel = Template.bind({});
RenderCustomLabel.args = {
  baseNodeId: 'ACL-PORTAL-DEV^SPRING_BOOT',
  data: getServerMapData(),
  renderEdgeLabel: (edge) => {
    if (edge?.edges?.length) {
      return edge.edges.reduce((acc, curr) => {
        return acc + edgeDataById[curr.id].totalCount;
      }, 0)
    } else {
      return edgeDataById[edge.id].totalCount;
    }
  }
};
