import React from 'react';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { FaNetworkWired, FaChartLine, FaCog, FaUserCog, FaUserCircle } from 'react-icons/fa';

import { Nav, SideNavigation } from '@pinpoint-fe/ui';

interface LayoutWithSideNavigationProps {
  children: React.ReactNode;
}

export const LayoutWithSideNavigation = ({
  children,
}: LayoutWithSideNavigationProps) => {
  function handleClickScaleButton({ small }: { small: boolean }) {
    // TODO save localstorage
  }

  return (
    <StyledContainer>
      <div>
        <SideNavigation
          isSmall={false}
          onClickScaleButton={handleClickScaleButton}
        >
          <Nav.Item
            href={'/serverMap'}
            icon={<FaNetworkWired />}
          >
            Servermap
          </Nav.Item>
          <Nav.Item
            href={'/inspector'}
            icon={<FaChartLine />}
          >
            Inspector
          </Nav.Item>
          <BottomAlignedMenus>
            <Nav.Menu
              title='Configuration'
              icon={<FaCog />}
            >
              <Nav.MenuItem href={'/config/userGroup'}>User Group</Nav.MenuItem>
              <Nav.MenuItem href={'/config/alarm'}>Alarm</Nav.MenuItem>
              <Nav.MenuItem href={'/config/webhook'}>Webhook</Nav.MenuItem>
              <Nav.MenuItem href={'/config/installation'}>Installation</Nav.MenuItem>
              <Nav.Divider />
              <Nav.MenuItem href={'/config/help'}>Help</Nav.MenuItem>
              <Nav.MenuItem href={'http://github.com/naver/pinpoint'}>Github</Nav.MenuItem>
              <Nav.Divider />
              <Nav.MenuItem href={'/config/experimental'}>Experimental</Nav.MenuItem>
            </Nav.Menu>
            <Nav.Menu
              title='Administration'
              icon={<FaUserCog />}
            >
              <Nav.MenuItem href={'/config/agentStatistic'}>Agent Statistic</Nav.MenuItem>
              <Nav.MenuItem href={'/config/agentManagement'}>Agent management</Nav.MenuItem>
            </Nav.Menu>
            <Nav.Menu
              title='User'
              icon={<FaUserCircle />}
            >
              <Nav.MenuItem href={'/config/general'}>General</Nav.MenuItem>
              <Nav.MenuItem href={'/config/favorite'}>Favorite List</Nav.MenuItem>
              <Nav.Divider />
              <Nav.MenuItem>Theme</Nav.MenuItem>
            </Nav.Menu>
          </BottomAlignedMenus>
        </SideNavigation>
      </div>
      <div css={css`flex:1;`}>
        {children}
      </div>
    </StyledContainer>
  );
};

const StyledContainer = styled.div`
  display: flex;
`

const BottomAlignedMenus = styled.div`
  margin-top: auto;
`

export const getLayoutWithSideNavigation = (page: React.ReactNode) => {
  return (
    <LayoutWithSideNavigation>
      {page}
    </LayoutWithSideNavigation>
  )
}


