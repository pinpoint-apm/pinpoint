import { useAtomValue } from 'jotai';
import { APP_PATH, MenuItemType as MenuItem } from '@pinpoint-fe/ui/src/constants';
import {
  configurationAtom,
  DEFAULT_SERVICE,
  searchParametersAtom,
} from '@pinpoint-fe/ui/src/atoms';
import {
  useEnableServiceMap,
  useIsDefaultService,
  useServiceNameForLink,
} from '@pinpoint-fe/ui/src/hooks';
import {
  PiBugBeetle,
  PiChartBar,
  PiChartLine,
  PiHardDrives,
  PiTreeStructure,
} from 'react-icons/pi';
import { SiOpentelemetry } from 'react-icons/si';
import {
  getServerMapPath,
  getServiceMapPath,
  getInspectorPath,
  getUrlStatPath,
  getSystemMetricPath,
  getErrorAnalysisPath,
  getOpenTelemetryPath,
} from '@pinpoint-fe/ui/src/utils';

export const useMenuItems = () => {
  const configuration = useAtomValue(configurationAtom);
  const { application, searchParameters } = useAtomValue(searchParametersAtom);
  // servicemap 링크는 어떤 service를 볼지 경로에 담아야 한다. 지금 보고 있는 화면의 service를
  // 그대로 이어받는다. (serviceName이 실린 경로면 그 값, 아니면 전역 선택값)
  const serviceName = useServiceNameForLink() ?? DEFAULT_SERVICE;
  const isDefaultService = useIsDefaultService();
  const enableServiceMap = useEnableServiceMap();

  const menuItems: MenuItem[] = [
    {
      icon: <PiTreeStructure />,
      name: 'Servermap',
      path: APP_PATH.SERVER_MAP,
      href: getServerMapPath(application, searchParameters),
    },
    {
      icon: <PiTreeStructure />,
      name: 'Servicemap',
      path: APP_PATH.SERVICE_MAP,
      // DEFAULT가 아닌 service는 소속 application을 모두 모아 그리므로 application을 싣지 않는다.
      // 실어 보내면 페이지가 곧 경로에서 지우면서 한 번 더 이동한다.
      href: getServiceMapPath(serviceName, isDefaultService ? application : null, searchParameters),
      // service 기능(experimental.enableServiceMap)이 켜져 있을 때만 노출한다.
      hide: !enableServiceMap,
    },
    {
      icon: <PiChartLine />,
      name: 'Inspector',
      path: APP_PATH.INSPECTOR,
      href: getInspectorPath(application, searchParameters),
      hide: !configuration?.showInspector,
    },
    {
      icon: <PiChartBar />,
      name: 'URL Statistic',
      path: APP_PATH.URL_STATISTIC,
      href: getUrlStatPath(application, searchParameters),
      hide: !configuration?.showUrlStat,
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
    {
      icon: <PiHardDrives />,
      name: 'Infrastructure',
      path: APP_PATH.SYSTEM_METRIC,
      href: getSystemMetricPath(),
      hide: !configuration?.showSystemMetric,
    },
  ];

  return { menuItems };
};
