import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { useConfiguration, useEnableServiceMap } from '@pinpoint-fe/ui/src/hooks';
import { Separator } from '../../components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../../components/ui/tabs';
import { t } from 'i18next';
import React from 'react';
import { useLocation, useNavigate } from 'react-router';
export interface LayoutWithAlarmProps {
  children?: React.ReactNode;
}

export const LayoutWithAlarm = ({ children }: LayoutWithAlarmProps) => {
  const configuration = useConfiguration();
  const enableServiceMap = useEnableServiceMap();
  const { pathname } = useLocation();
  const navigate = useNavigate();

  // servicemap이 켜지면 두 화면 모두 service 단위 경로로 옮겨진다(`HIDDEN_PAGE_RULES`의
  // Alarm·Webhook 쌍). 탭 값은 **지금 보이는 쪽의 경로**여야 한다 — 두 가지가 걸려 있다.
  // ① 아래 `defaultValue={pathname}`가 탭 값과 맞아떨어져야 `TabsContent`가 그려진다.
  //    옛 경로를 두면 `/config/service/alarm`에서 탭만 보이고 내용이 통째로 비어 버린다.
  // ② 탭을 누르면 그 경로로 이동한다. 옛 경로로 보내면 감춰진 화면으로 나갔다가 다시 끌려와
  //    service 밖의 주소가 한 번 스쳐 지나간다.
  const alarmTabs = [
    {
      id: 'alarm',
      text: 'Alarms',
      path: enableServiceMap ? APP_PATH.CONFIG_SERVICE_ALARM : APP_PATH.CONFIG_ALARM,
    },
    {
      id: 'webhook',
      text: 'Webhook',
      path: enableServiceMap ? APP_PATH.CONFIG_SERVICE_WEBHOOK : APP_PATH.CONFIG_WEBHOOK,
    },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h3 className="text-lg font-semibold">Alarms</h3>
        <div className="text-sm text-muted-foreground">
          {t('CONFIGURATION.ALARM.DESC')
            .split('\n')
            .map((txt, i) => (
              <p key={i}>{txt}</p>
            ))}
        </div>
      </div>
      {configuration?.webhookEnable ? (
        <Tabs defaultValue={pathname}>
          <TabsList>
            {alarmTabs.map((tab) => (
              <TabsTrigger key={tab.id} value={tab.path} onClick={() => navigate(tab.path)}>
                {tab.text}
              </TabsTrigger>
            ))}
          </TabsList>
          {alarmTabs.map((tab) => (
            <TabsContent key={tab.id} value={tab.path}>
              <Separator className="mb-6" />
              {children}
            </TabsContent>
          ))}
        </Tabs>
      ) : (
        <>
          <Separator className="mb-6" />
          {children}
        </>
      )}
    </div>
  );
};
