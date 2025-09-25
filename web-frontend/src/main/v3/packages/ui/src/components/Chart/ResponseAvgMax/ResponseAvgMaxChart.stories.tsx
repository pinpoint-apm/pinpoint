import { StoryFn, Meta } from '@storybook/react';
import React from 'react';
import { ResponseAvgMaxChart } from './ResponseAvgMaxChart';

export default {
  title: 'PINPOINT/Component/Charts/ResponseAvgMaxChart',
  component: ResponseAvgMaxChart,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as Meta<typeof ResponseAvgMaxChart>;

const Template: StoryFn<typeof ResponseAvgMaxChart> = (args) => <ResponseAvgMaxChart {...args} />;

export const Default = Template.bind({});
Default.args = {
  data: [300, 1],
};
export const NoData = Template.bind({});
NoData.args = {};
