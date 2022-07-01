import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import { WrapLink } from './WrapLink';

export default {
  title: 'PINPOINT/Component/BASE/WrapLink',
  component: WrapLink,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof WrapLink>;

const Template: ComponentStory<typeof WrapLink> = (args) => <WrapLink {...args} />;

export const Default = Template.bind({});
Default.args = {
  path: '',
  children: 'default no path'
};
