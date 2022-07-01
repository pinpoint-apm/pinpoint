import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

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
    <Nav.Menu title={'Configuration'}>
      <Nav.Item>
        User Group
      </Nav.Item>
      <Nav.Item>
        Authentication & Alarm
      </Nav.Item>
      <Nav.Item>
        Webhook
      </Nav.Item>
      <Nav.Item>
        Installation
      </Nav.Item>
      <Nav.Item>
        Help
      </Nav.Item>
      <Nav.Item>
        Yobi
      </Nav.Item>
      <Nav.Item>
        Experimental
      </Nav.Item>
    </Nav.Menu>
  </SideNavigation>
)

export const Default = Template.bind({});
Default.args = {
};
