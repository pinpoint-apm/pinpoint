import { useAtomValue } from 'jotai';
import { ExperimentalPage as Experimental, withInitialFetch } from '@pinpoint-fe/ui';
import {
  getLayoutWithConfiguration,
  getLayoutWithSideNavigation,
} from '@pinpoint-fe/web/src/components/Layout';
import { configurationAtom } from '@pinpoint-fe/ui/src/atoms';

export interface ExperimentalPageProps {}
const ExperimentalPage = () => {
  const configuration = useAtomValue(configurationAtom);

  return <Experimental configuration={configuration} />;
};

export default withInitialFetch((props: ExperimentalPageProps) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<ExperimentalPage {...props} />)),
);
