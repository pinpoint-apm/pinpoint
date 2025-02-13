import {
  getLayoutWithConfiguration,
  getLayoutWithSideNavigation,
} from '@pinpoint-fe/web/src/components/Layout';
import {
  AgentDuplicationCheck,
  ApplicationDuplicationCheck,
  InstallationPage as CommonInstallationPage,
  Download,
  JvmArgumentInfo,
  withInitialFetch,
} from '@pinpoint-fe/ui';

export interface InstallationPageProps {}

const InstallationPage = () => {
  const installationItemList = [
    {
      label: 'Application Name',
      renderer: <ApplicationDuplicationCheck />,
    },
    {
      label: 'Agent ID',
      renderer: <AgentDuplicationCheck />,
    },
    {
      label: 'JVM Argument Info',
      renderer: <JvmArgumentInfo />,
    },
    {
      label: 'Download',
      renderer: <Download />,
    },
  ];

  return <CommonInstallationPage installationItemList={installationItemList} />;
};

export default withInitialFetch(() =>
  getLayoutWithSideNavigation(getLayoutWithConfiguration(<InstallationPage />)),
);
