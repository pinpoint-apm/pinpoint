import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import { NextLink } from './NextLink';

export default {
  title: 'PINPOINT/Component/BASE/NextLink',
  component: NextLink,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof NextLink>;

const Template: ComponentStory<typeof NextLink> = (args) => <NextLink {...args} />;

export const Default = Template.bind({});
Default.args = {
  href: '',
  children: 'default no path'
};
