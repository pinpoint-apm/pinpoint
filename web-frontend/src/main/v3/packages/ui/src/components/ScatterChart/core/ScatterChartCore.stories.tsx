import React from 'react';
import { StoryFn, Meta } from '@storybook/react';
import { ScatterChartCore } from './ScatterChartCore';

export default {
  title: 'PINPOINT/Component/ScatterChart',
  component: ScatterChartCore,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as Meta<typeof ScatterChartCore>;

const Template: StoryFn<typeof ScatterChartCore> = (args) => <ScatterChartCore {...args} />;

export const Default = Template.bind({});
Default.args = {};
