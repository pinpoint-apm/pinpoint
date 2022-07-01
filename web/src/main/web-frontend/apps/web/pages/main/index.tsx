import React from 'react';

import { StyledMainHeader } from '@pinpoint-fe/common/components/Styled/header';
import { NextPageWithLayout } from '../../pages/_app';
import { getLayoutWithSideNavigation } from '../../components/Layout/LayoutWithSideNavigation';


export interface mainProps {
}

const Main: NextPageWithLayout = ({

}: mainProps) => {
  return (
    <div>
      <StyledMainHeader>
        main page
      </StyledMainHeader>
    </div>
  )
}

Main.getLayout = (page) => getLayoutWithSideNavigation(page);

export default Main;
