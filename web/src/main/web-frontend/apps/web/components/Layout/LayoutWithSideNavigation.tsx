import React from 'react';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { FaNetworkWired, FaChartLine, FaCog, FaUserCog, FaUserCircle } from 'react-icons/fa';

import { SideNavigation } from '@pinpoint-fe/common/components/SideNavigation/SideNavigation';
import Nav from '@pinpoint-fe/common/components/SideNavigation/Nav';

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
            path={'/main'}
            icon={<FaNetworkWired />}
          >
            Servermap
          </Nav.Item>
          <Nav.Item
            path={'/inspector'}
            icon={<FaChartLine />}
          >
            Inspector
          </Nav.Item>
          <BottomAlignedMenus>
            <Nav.Menu
              title='Configuration'
              icon={<FaCog />}
            >
              <Nav.MenuItem path={'/config/userGroup'}>User Group</Nav.MenuItem>
              <Nav.MenuItem path={'/config/alarm'}>Alarm</Nav.MenuItem>
              <Nav.MenuItem path={'/config/webhook'}>Webhook</Nav.MenuItem>
              <Nav.MenuItem path={'/config/installation'}>Installation</Nav.MenuItem>
              <Nav.Divider />
              <Nav.MenuItem path={'/config/help'}>Help</Nav.MenuItem>
              <Nav.MenuItem path={'http://github.com/naver/pinpoint'}>Github</Nav.MenuItem>
              <Nav.Divider />
              <Nav.MenuItem path={'/config/experimental'}>Experimental</Nav.MenuItem>
            </Nav.Menu>
            <Nav.Menu
              title='Administration'
              icon={<FaUserCog />}
            >
              <Nav.MenuItem path={'/config/agentStatistic'}>Agent Statistic</Nav.MenuItem>
              <Nav.MenuItem path={'/config/agentManagement'}>Agent management</Nav.MenuItem>
            </Nav.Menu>
            <Nav.Menu
              title='User'
              icon={<FaUserCircle />}
            >
              <Nav.MenuItem path={'/config/general'}>General</Nav.MenuItem>
              <Nav.MenuItem path={'/config/favorite'}>Favorite List</Nav.MenuItem>
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


