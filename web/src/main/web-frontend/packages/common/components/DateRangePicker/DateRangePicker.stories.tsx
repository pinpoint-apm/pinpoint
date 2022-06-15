import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import { DateRangePicker } from './DateRangePicker';

export default {
  title: 'PINPOINT/Component/BASE/DateRangePicker1',
  component: DateRangePicker,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof DateRangePicker>;

const Template: ComponentStory<typeof DateRangePicker> = (args) => <DateRangePicker {...args} />;

export const Default = Template.bind({});
Default.args = {
};
