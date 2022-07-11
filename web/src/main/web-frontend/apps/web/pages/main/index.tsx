import React from 'react';

import { NextPageWithLayout } from '../../pages/_app';
import { getLayoutWithSideNavigation } from '../../components/Layout/LayoutWithSideNavigation';
import { StyledMainHeader } from '@pinpoint-fe/common/components/Styled/header';
import { ApplicationSelector } from '../../components/ApplicationSelector/ApplicationSelector';


export interface mainProps {
}

const Main: NextPageWithLayout = ({

}: mainProps) => {
  return (
    <div>
      <StyledMainHeader>
        <ApplicationSelector />
      </StyledMainHeader>
    </div>
  )
}

Main.getLayout = (page) => getLayoutWithSideNavigation(page);

export default Main;
