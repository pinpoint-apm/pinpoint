import React from 'react';
import styled from '@emotion/styled';

import { StyledMainHeader } from '@pinpoint-fe/ui';
import { NextPageWithLayout } from '../_app';
import { getLayoutWithSideNavigation } from '../../components/Layout/LayoutWithSideNavigation';
import { ApplicationSelector } from '../../components/ApplicationSelector/ApplicationSelector';
import { useRouter } from 'next/router';
import { getServerMapPath } from '../../utils/path';

export interface ServerMapInactiveProps {
}

const ServerMapInactive: NextPageWithLayout = ({

}: ServerMapInactiveProps) => {
  const router = useRouter();
  
  // const { setApplication } = useApplication({ basePath: '/serverMap' });

  return (
    <StyledContainer>
      <StyledMainHeader>
        <ApplicationSelector
          onClick={({ application }) => router.push(getServerMapPath(application))}
        />
      </StyledMainHeader>
      <StyledMainContainer>
        Select your application.
      </StyledMainContainer>
    </StyledContainer>
  )
}

const StyledContainer = styled.div`
  display: flex;
  flex: 1;
  flex-direction: column;
  height: 100%;
`

const StyledMainContainer = styled.div`
  flex:1;
  display: grid;
  grid-template-columns: auto 500px;
`

ServerMapInactive.getLayout = (page) => getLayoutWithSideNavigation(page);

export default ServerMapInactive;
