import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import { ServerMapOptionDropdown } from './ServerMapOptionDropdown';

export default {
  title: 'PINPOINT/Component/ServerMapOptionDropdown',
  component: ServerMapOptionDropdown,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof ServerMapOptionDropdown>;

const Template: ComponentStory<typeof ServerMapOptionDropdown> = (args) => <ServerMapOptionDropdown {...args} />;

export const Default = Template.bind({});
Default.args = {
};
