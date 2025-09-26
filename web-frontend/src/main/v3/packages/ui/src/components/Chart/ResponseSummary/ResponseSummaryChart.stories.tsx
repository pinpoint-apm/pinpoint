import React from 'react';
import { StoryFn, Meta } from '@storybook/react';
import { ResponseSummaryChart } from './ResponseSummaryChart';

export default {
  title: 'PINPOINT/Component/Charts/ResponseSummaryChart',
  component: ResponseSummaryChart,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as Meta<typeof ResponseSummaryChart>;

const Template: StoryFn<typeof ResponseSummaryChart> = (args) => <ResponseSummaryChart {...args} />;

export const Default = Template.bind({});
Default.args = {
  data: [300, 350, 300, 150, 100],
};

export const NoData = Template.bind({});
NoData.args = {
  // data: [300, 350, 300, 150, 100]
};
