import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';
import { FaReact } from 'react-icons/fa';
import { SiRiotgames } from 'react-icons/si';

import { SideNavigation } from './SideNavigation';
import Nav from './Nav';

export default {
  title: 'PINPOINT/Component/BASE/SideNavigation',
  component: SideNavigation,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof SideNavigation>;

const Template: ComponentStory<typeof SideNavigation> = (args) => (
  <SideNavigation {...args}>
    <Nav.Item icon={<FaReact />}>Sample Single</Nav.Item>
    <Nav.Menu icon={<SiRiotgames />} title={'Sample Menu'}>
      <Nav.MenuItem>
        Item 1
      </Nav.MenuItem>
      <Nav.MenuItem>
        Item 2
      </Nav.MenuItem>
      <Nav.Divider />
      <Nav.MenuItem>
        👆🏻 Divider
      </Nav.MenuItem>
      <Nav.MenuItem>
        Last Item
      </Nav.MenuItem>
    </Nav.Menu>
  </SideNavigation>
)

export const Default = Template.bind({});
Default.args = {
};
