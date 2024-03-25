import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import { Drawer } from './Drawer';

export default {
  title: 'PINPOINT/Component/Drawer',
  component: Drawer,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof Drawer>;

const Template: ComponentStory<typeof Drawer> = (args) => <Drawer {...args} />;

export const Default = Template.bind({});
Default.args = {};
