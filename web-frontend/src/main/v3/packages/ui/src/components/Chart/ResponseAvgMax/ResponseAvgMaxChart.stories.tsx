import { ComponentStory, ComponentMeta } from '@storybook/react';

import { ResponseAvgMaxChart } from './ResponseAvgMaxChart';

export default {
  title: 'PINPOINT/Component/Charts/ResponseAvgMaxChart',
  component: ResponseAvgMaxChart,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof ResponseAvgMaxChart>;

const Template: ComponentStory<typeof ResponseAvgMaxChart> = (args) => (
  <ResponseAvgMaxChart {...args} />
);

export const Default = Template.bind({});
Default.args = {
  data: [300, 1],
};
export const NoData = Template.bind({});
NoData.args = {};
