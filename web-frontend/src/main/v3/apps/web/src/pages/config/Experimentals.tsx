import { useAtomValue } from 'jotai';
import { ExperimentalPage as Experimental, withInitialFetch } from '@pinpoint-fe/ui';
import { getLayoutWithConfiguration, getLayoutWithSideNavigation } from '@/components/Layout';
import { configurationAtom } from '@pinpoint-fe/ui/atoms';

export interface ExperimentalPageProps {}
const ExperimentalPage = () => {
  const configuration = useAtomValue(configurationAtom);

  return <Experimental configuration={configuration} />;
};

export default withInitialFetch((props: ExperimentalPageProps) =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<ExperimentalPage {...props} />)),
);
