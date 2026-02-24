import React from 'react';
import { Outlet } from 'react-router-dom';
import { LayoutWithSideNavigation } from './LayoutWithSideNavigation';

export const SideNavigationOutlet = () => {
  return (
    <LayoutWithSideNavigation>
      <React.Suspense fallback={null}>
        <Outlet />
      </React.Suspense>
    </LayoutWithSideNavigation>
  );
};
