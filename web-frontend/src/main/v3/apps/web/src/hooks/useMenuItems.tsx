import { useAtomValue } from 'jotai';
import { APP_PATH, MenuItem } from '@pinpoint-fe/constants';
import { configurationAtom } from '@pinpoint-fe/atoms';
import {
  PiBugBeetle,
  PiChartBar,
  PiChartBarHorizontal,
  PiChartLine,
  PiHardDrives,
  PiTreeStructure,
} from 'react-icons/pi';
import { useSearchParameters } from '@pinpoint-fe/hooks';
import {
  getServerMapPath,
  getInspectorPath,
  getUrlStatPath,
  getSystemMetricPath,
  getErrorAnalysisPath,
  getOpenTelemetryPath,
} from '@pinpoint-fe/utils';

export const useMenuItems = () => {
  const configuration = useAtomValue(configurationAtom);
  const { application, searchParameters } = useSearchParameters();

  const menuItems: MenuItem[] = [
    {
      icon: <PiTreeStructure />,
      name: 'Servermap',
      path: APP_PATH.SERVER_MAP,
      href: getServerMapPath(application, searchParameters),
    },
    {
      icon: <PiChartLine />,
      name: 'Inspector',
      path: APP_PATH.INSPECTOR,
      href: getInspectorPath(application, searchParameters),
      hide: !configuration?.showV3Inspector,
    },
    {
      icon: <PiChartBar />,
      name: 'URL Statistic',
      path: APP_PATH.URL_STATISTIC,
      href: getUrlStatPath(application, searchParameters),
      hide: !configuration?.showUrlStat,
    },
    {
      icon: <PiHardDrives />,
      name: 'Infrastructure',
      path: APP_PATH.SYSTEM_METRIC,
      href: getSystemMetricPath(),
      hide: !configuration?.showSystemMetric,
    },
    {
      icon: <PiBugBeetle />,
      name: 'Error Analysis',
      path: APP_PATH.ERROR_ANALYSIS,
      href: getErrorAnalysisPath(application, searchParameters),
      hide: !configuration?.showExceptionTrace,
    },
    {
      icon: <PiChartBarHorizontal />,
      name: 'Open Telemetry',
      path: APP_PATH.OPEN_TELEMETRY,
      href: getOpenTelemetryPath(application, searchParameters),
      hide: true, // TODO: Bind with Open Telemetry config
    },
  ];

  return { menuItems };
};
