import React from 'react';

import { NextPageWithLayout } from '../../pages/_app';
import { getLayoutWithSideNavigation } from '../../components/Layout/LayoutWithSideNavigation';
import { ApplicationSelector } from '../../components/ApplicationSelector/ApplicationSelector';
import { DateRangePicker, StyledMainHeader } from '@pinpoint-fe/ui';
import styled from '@emotion/styled';

export interface mainProps {

}

const Main: NextPageWithLayout = ({

}: mainProps) => {
  return (
    <StyledContainer>
      <StyledHeader>
        <ApplicationSelector />
        <DateRangePicker />
      </StyledHeader>
      <StyledMainContainer>
        <div>contents</div>
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

const StyledHeader = styled(StyledMainHeader)`
  gap: 10px;
`

Main.getLayout = (page) => getLayoutWithSideNavigation(page);

export default Main;
