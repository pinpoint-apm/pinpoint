import { ComponentStory, ComponentMeta } from '@storybook/react';

import { ScatterChartCore } from './ScatterChartCore';

export default {
  title: 'PINPOINT/Component/ScatterChart',
  component: ScatterChartCore,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof ScatterChartCore>;

const Template: ComponentStory<typeof ScatterChartCore> = (args) => <ScatterChartCore {...args} />;

export const Default = Template.bind({});
Default.args = {};
