import {
  AgentDuplicationCheck,
  ApplicationDuplicationCheck,
  InstallationPage as CommonInstallationPage,
  Download,
  JvmArgumentInfo,
} from '@pinpoint-fe/ui';

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

export default function Installation() {
  return <CommonInstallationPage installationItemList={installationItemList} />;
}
