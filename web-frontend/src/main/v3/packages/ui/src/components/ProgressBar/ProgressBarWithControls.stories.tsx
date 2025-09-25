import React from 'react';
import { StoryFn, Meta } from '@storybook/react';
import { ProgressBarWithControls } from './ProgressBarWithControls';

export default {
  title: 'PINPOINT/Component/Common/ProgressBarWithControls',
  component: ProgressBarWithControls,
  argTypes: {
    complete: {
      control: 'boolean',
    },
    backgroundColor: { control: 'color' },
  },
} as Meta<typeof ProgressBarWithControls>;

const Template: StoryFn<typeof ProgressBarWithControls> = (args) => (
  <ProgressBarWithControls {...args} />
);

export const Default = Template.bind({});
Default.args = {};
