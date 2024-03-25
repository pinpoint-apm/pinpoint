import { ComponentStory, ComponentMeta } from '@storybook/react';

import {
  Menu,
  MenuItem,
  SidebarProvider,
  Sidebar,
  menuItemStyles,
  bottomMenuItemStyles,
} from './Sidebar';
import {
  FaNetworkWired,
  FaChartLine,
  FaChartBar,
  FaServer,
  FaCog,
  FaUserCircle,
} from 'react-icons/fa';
import { SubMenu } from 'react-pro-sidebar';

export default {
  title: 'PINPOINT-UI/Components/Sidebar',
  component: Sidebar,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof Sidebar>;

const Template: ComponentStory<typeof Sidebar> = (args) => {
  return (
    <SidebarProvider {...args}>
      <div style={{ display: 'flex', height: '100vh' }}>
        <Sidebar>
          <Menu menuItemStyles={menuItemStyles}>
            <MenuItem icon={<FaNetworkWired />}>Servermap</MenuItem>
            <MenuItem icon={<FaChartLine />}>Inspector</MenuItem>
            <MenuItem icon={<FaChartBar />}>URL Statistic</MenuItem>
            <MenuItem icon={<FaServer />}>Infrastructure</MenuItem>
          </Menu>
          <Menu
            style={{ marginTop: 'auto' }}
            menuItemStyles={{
              ...menuItemStyles,
              ...bottomMenuItemStyles,
            }}
          >
            <SubMenu icon={<FaCog />} label="Configuration">
              <MenuItem>User Group</MenuItem>
              <MenuItem>Authentication & Alarm</MenuItem>
              <MenuItem>Webhook</MenuItem>
              <MenuItem>Installation</MenuItem>
              <MenuItem>Help</MenuItem>
              <MenuItem>Yobi</MenuItem>
              <MenuItem>Experimental</MenuItem>
            </SubMenu>
            <SubMenu icon={<FaUserCircle />} label="User">
              <MenuItem>General</MenuItem>
              <MenuItem>User Profile</MenuItem>
              <MenuItem>Log Out</MenuItem>
              <MenuItem>Theme</MenuItem>
            </SubMenu>
          </Menu>
        </Sidebar>
      </div>
    </SidebarProvider>
  );
};

export const Default = Template.bind({});
Default.args = {};
