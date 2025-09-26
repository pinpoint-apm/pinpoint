import React from 'react';
import { StoryFn, Meta } from '@storybook/react';
import { Drawer } from './Drawer';

export default {
  title: 'PINPOINT/Component/Drawer',
  component: Drawer,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as Meta<typeof Drawer>;

const Template: StoryFn<typeof Drawer> = (args) => <Drawer {...args} />;

export const Default = Template.bind({});
Default.args = {};
