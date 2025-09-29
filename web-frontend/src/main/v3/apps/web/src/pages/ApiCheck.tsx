import { AiFillApi } from 'react-icons/ai';

import { MainHeader, ApiCheck as ApiCheckComponent } from '@pinpoint-fe/ui';
import { getLayoutWithSideNavigation } from '@pinpoint-fe/web/src/components/Layout/LayoutWithSideNavigation';

export const ApiCheck = () => {
  return (
    <>
      <MainHeader
        title={
          <div className="flex items-center gap-2">
            <AiFillApi />
            Api Check
          </div>
        }
      ></MainHeader>
      <ApiCheckComponent />
    </>
  );
};

export default () => getLayoutWithSideNavigation(<ApiCheck />);
