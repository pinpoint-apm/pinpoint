import { Outlet } from 'react-router-dom';
import { LayoutWithConfiguration } from './LayoutWithConfiguration';

export const ConfigurationOutlet = () => {
  return (
    <LayoutWithConfiguration>
      <Outlet />
    </LayoutWithConfiguration>
  );
};
