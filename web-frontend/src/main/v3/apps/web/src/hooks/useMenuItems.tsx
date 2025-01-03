import { useAtomValue } from 'jotai';
import { APP_PATH, MenuItemType as MenuItem } from '@pinpoint-fe/ui/constants';
import { configurationAtom } from '@pinpoint-fe/ui/atoms';
import {
  PiBugBeetle,
  PiChartBar,
  PiChartLine,
  PiHardDrives,
  PiTreeStructure,
} from 'react-icons/pi';
import { SiOpentelemetry } from 'react-icons/si';
import { useSearchParameters } from '@pinpoint-fe/ui/hooks';
import {
  getServerMapPath,
  getInspectorPath,
  getUrlStatPath,
  getSystemMetricPath,
  getErrorAnalysisPath,
  getOpenTelemetryPath,
} from '@pinpoint-fe/ui/utils';

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
      icon: <SiOpentelemetry />,
      name: 'OpenTelemetry Metric',
      path: APP_PATH.OPEN_TELEMETRY_METRIC,
      href: getOpenTelemetryPath(application, searchParameters),
      hide: !configuration?.showOtlpMetric,
    },
  ];

  return { menuItems };
};
