import { Outlet } from 'react-router';
import { LayoutWithConfiguration } from './LayoutWithConfiguration';

export const ConfigurationOutlet = () => {
  return (
    <LayoutWithConfiguration>
      <Outlet />
    </LayoutWithConfiguration>
  );
};
