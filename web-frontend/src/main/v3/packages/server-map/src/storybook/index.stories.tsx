import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import { ServerMap } from '../ui/ServerMap';
import { getServerMapData } from './mock/util';
import data1 from './mock/data1.json';
import data2 from './mock/data2.json';
import data3 from './mock/data3.json';
import data4 from './mock/data4.json';
import data5 from './mock/data5.json';

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

const DefaultTemplate: ComponentStory<typeof ServerMap> = (args) => {
  const [data, setData] = React.useState(data1);

  return (
    <div>
      <div style={{ width: '100%', height: '90vh', border: '1px solid black' }}>
        <ServerMap
          {...args}
          data={getServerMapData(data)}
          renderEdgeLabel={(edge) => {
            if (edge?.edges?.length) {
              return edge.edges.reduce((acc, curr) => {
                return acc + curr?.transactionInfo?.totalCount;
              }, 0);
            }
            return edge?.transactionInfo?.totalCount;
          }}
        />
      </div>
      <button onClick={() => setData(data1)}>change data1</button>
      <button onClick={() => setData(data2)}>change data2</button>
      <button onClick={() => setData(data3)}>change data3</button>
      <button onClick={() => setData(data4)}>change data4</button>
      <button onClick={() => setData(data5)}>change data5</button>
    </div>
  );
};

export const Default = DefaultTemplate.bind({});
Default.args = {
  baseNodeId: 'ApiGateway^SPRING_BOOT',
};

const RenderCustomLabelTemplate: ComponentStory<typeof ServerMap> = (args) => (
  <div style={{ width: '100%', height: '90vh', border: '1px solid black' }}>
    <ServerMap {...args} />
  </div>
);

export const RenderCustomLabel = RenderCustomLabelTemplate.bind({});
RenderCustomLabel.args = {
  baseNodeId: 'ApiGateway^SPRING_BOOT',
  data: getServerMapData(data1),
  renderEdgeLabel: (edge) => {
    const edgeData = edge.transactionInfo;

    return `${edgeData?.totalCount} (${edgeData?.avg}ms)`;
  },
};

const MergedNodeTemplate: ComponentStory<typeof ServerMap> = (args) => (
  <div style={{ width: '100%', height: '90vh', border: '1px solid black' }}>
    <ServerMap {...args} />
  </div>
);

export const MergedNode = MergedNodeTemplate.bind({});
MergedNode.args = {
  baseNodeId: 'ACL-PORTAL-DEV^SPRING_BOOT',
  data: getServerMapData(data4),
  renderEdgeLabel: (edge) => {
    if (edge?.edges?.length) {
      return 'This is merged';
    } else {
      return '';
    }
  },
};
