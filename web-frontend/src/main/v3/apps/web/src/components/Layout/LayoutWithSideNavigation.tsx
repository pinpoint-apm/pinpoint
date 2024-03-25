import React from 'react';
import {
  LayoutWithSideNavigation as LayoutWithSideNavigationComponent,
  LayoutWithSideNavigationProps as LayoutWithSideNavigationComponentProps,
  SideNavigationMenuItem,
} from '@pinpoint-fe/ui';
import {
  FaBug,
  FaChartBar,
  // FaChartLine,
  FaCog,
  FaNetworkWired,
  FaServer,
} from 'react-icons/fa';
import { useSearchParameters } from '@pinpoint-fe/hooks';
import {
  getErrorAnalysisPath,
  // getInspectorPath,
  getServerMapPath,
  getSystemMetricPath,
  getUrlStatPath,
} from '@pinpoint-fe/utils';
import { APP_PATH } from '@pinpoint-fe/constants';
import { useAtomValue } from 'jotai';
import { LuDoorOpen, LuUserCircle2 } from 'react-icons/lu';
import { CONFIG_MENU_MAP } from './LayoutWithConfiguration';
import { MdOutlineAdminPanelSettings } from 'react-icons/md';
import { configurationAtom } from '@pinpoint-fe/atoms';

export interface LayoutWithSideNavigationProps extends LayoutWithSideNavigationComponentProps {}

export const LayoutWithSideNavigation = ({ ...props }: LayoutWithSideNavigationProps) => {
  const { application, searchParameters } = useSearchParameters();
  const configuration = useAtomValue(configurationAtom);

  const topMenuItems = [
    {
      icon: <FaNetworkWired />,
      name: 'Servermap',
      path: APP_PATH.SERVER_MAP,
      href: getServerMapPath(application, searchParameters),
      show: true,
    },
    // {
    //   icon: <FaChartLine />,
    //   name: 'Inspector',
    //   path: APP_PATH.INSPECTOR,
    //   href: getInspectorPath(application, searchParameters),
    // },
    {
      icon: <FaChartBar />,
      name: 'URL Statistic',
      path: APP_PATH.URL_STATISTIC,
      href: getUrlStatPath(application, searchParameters),
      show: true,
    },
    {
      icon: <FaServer />,
      name: 'Infrastructure',
      path: APP_PATH.SYSTEM_METRIC,
      href: getSystemMetricPath(),
      show: true,
    },
    {
      icon: <FaBug />,
      name: 'Error Analysis',
      path: APP_PATH.ERROR_ANALYSIS,
      href: getErrorAnalysisPath(application, searchParameters),
      show: configuration?.showExceptionTrace,
    },
  ];

  const bottomMenuItems: SideNavigationMenuItem[] = [
    {
      icon: <MdOutlineAdminPanelSettings />,
      name: CONFIG_MENU_MAP.ADMINISTRATION.title,
      path: APP_PATH.CONFIG_USERS,
      childItems: CONFIG_MENU_MAP.ADMINISTRATION.menus,
      show: true,
    },
    {
      icon: <FaCog />,
      name: CONFIG_MENU_MAP.CONFIGURATION.title,
      path: APP_PATH.CONFIG_USER_GROUP,
      childItems: CONFIG_MENU_MAP.CONFIGURATION.menus,
      show: true,
    },
    {
      icon: <LuUserCircle2 />,
      name: 'User',
      path: APP_PATH.CONFIG_GENERAL,
      childItems: CONFIG_MENU_MAP.PERSONAL_SETTINGS.menus,
      show: true,
    },
    {
      children: (collapsed) => (
        <div
          className="flex items-center justify-center gap-2 font-semibold"
          style={{ color: 'aqua' }}
        >
          {collapsed ? '' : 'Go to Pinpoint v2 '}
          <LuDoorOpen />
        </div>
      ),
      name: 'Go to Pinpoint v2',
      path: 'gotoV2',
      aHref: '/main',
      show: true,
    },
  ];

  return (
    <LayoutWithSideNavigationComponent
      topMenuItems={topMenuItems}
      bottomMenuItems={bottomMenuItems}
      {...props}
    />
  );
};

export const getLayoutWithSideNavigation = (page: React.ReactNode) => {
  return <LayoutWithSideNavigation>{page}</LayoutWithSideNavigation>;
};
