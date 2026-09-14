import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import {
  LayoutWithConfiguration as LayoutWithConfigurationComponent,
  LayoutWithConfigurationProps,
  SERVICE_CONFIG_MENU,
  findActiveConfigMenuGroup,
} from '@pinpoint-fe/ui';
import { useEnableServiceMap } from '@pinpoint-fe/ui/src/hooks';
import { useLocation } from 'react-router';
import { useAtomValue } from 'jotai';
import { useMemo } from 'react';
import { selectedServiceAtom } from '@pinpoint-fe/ui/src/atoms';

/**
 * 설정 화면들의 메뉴 묶음. `enableServiceMap`에 따라 감춰지는 항목이 있어 훅이다 —
 * 설정을 바꾸면 새로고침 없이 메뉴가 따라 바뀌어야 한다.
 */
export function useConfigMenuMap() {
  const enableServiceMap = useEnableServiceMap();

  return useMemo(
    () => ({
      CONFIGURATION: {
        title: 'Configuration',
        desc: 'Manage Pinpoint settings.',
        menus: [
          {
            name: 'User Group',
            path: APP_PATH.CONFIG_USER_GROUP,
            href: APP_PATH.CONFIG_USER_GROUP,
          },
          {
            name: 'Alarms',
            path: [APP_PATH.CONFIG_ALARM, APP_PATH.CONFIG_WEBHOOK],
            href: APP_PATH.CONFIG_ALARM,
            // servicemap이 켜지면 service 단위 Alarm으로 대체되므로 감춘다.
            hide: enableServiceMap,
          },
          {
            name: 'Installation',
            path: APP_PATH.CONFIG_INSTALLATION,
            href: APP_PATH.CONFIG_INSTALLATION,
          },
          {
            name: 'Help',
            path: APP_PATH.CONFIG_HELP,
            href: APP_PATH.CONFIG_HELP,
          },
          {
            name: 'Experimental',
            path: APP_PATH.CONFIG_EXPERIMENTAL,
            href: APP_PATH.CONFIG_EXPERIMENTAL,
          },
        ],
      },
      ADMINISTRATION: {
        title: 'Administration',
        desc: 'Manage admin settings.',
        menus: [
          {
            name: 'Users',
            path: APP_PATH.CONFIG_USERS,
            href: APP_PATH.CONFIG_USERS,
          },
          {
            name: 'Agent management',
            path: APP_PATH.CONFIG_AGENT_MANAGEMENT,
            href: APP_PATH.CONFIG_AGENT_MANAGEMENT,
          },
          {
            name: 'Agent statistic',
            path: APP_PATH.CONFIG_AGENT_STATISTIC,
            href: APP_PATH.CONFIG_AGENT_STATISTIC,
          },
        ],
      },
      PERSONAL_SETTINGS: {
        title: 'Personal Settings',
        desc: 'Manage personal settings.',
        menus: [
          {
            name: 'General',
            path: APP_PATH.CONFIG_GENERAL,
            href: APP_PATH.CONFIG_GENERAL,
          },
        ],
      },
      SERVICE: {
        ...SERVICE_CONFIG_MENU,
        // 설정이 꺼져 있으면 이 화면들은 Configuration 그룹의 것이다. 두 그룹이 같은 경로를
        // 나눠 가지므로, 어느 쪽이 감추지 않았는지가 곧 그 경로의 그룹이다.
        menus: SERVICE_CONFIG_MENU.menus.map((menu) => ({ ...menu, hide: !enableServiceMap })),
      },
    }),
    [enableServiceMap],
  );
}

export const LayoutWithConfiguration = ({ ...props }: LayoutWithConfigurationProps) => {
  const CONFIG_MENU_MAP = useConfigMenuMap();
  const { pathname } = useLocation();
  const selectedService = useAtomValue(selectedServiceAtom);

  const serviceConfigMenu = {
    ...CONFIG_MENU_MAP.SERVICE,
    title: `${CONFIG_MENU_MAP.SERVICE.title} (${selectedService})`,
  };

  const configMenu =
    pathname === APP_PATH.CONFIG_SERVICE_SETTING
      ? { ...serviceConfigMenu, menus: [] }
      : findActiveConfigMenuGroup(Object.values(CONFIG_MENU_MAP), pathname);

  return (
    <LayoutWithConfigurationComponent
      // Service 그룹은 제목에 지금 고른 service 이름을 붙여 보여준다.
      configMenu={configMenu === CONFIG_MENU_MAP.SERVICE ? serviceConfigMenu : configMenu}
      {...props}
    />
  );
};
