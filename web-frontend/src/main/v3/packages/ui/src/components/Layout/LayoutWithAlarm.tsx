import { APP_PATH } from '@pinpoint-fe/ui/src/constants';
import { useConfiguration } from '@pinpoint-fe/ui/src/hooks';
import { withoutTrailingSlash } from '@pinpoint-fe/ui/src/utils';
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
  // 라우터는 `/config/alarm/`도 이 화면으로 매칭한다. 탭 값과 정확히 비교되므로 끝의 '/'를
  // 떼지 않으면 어느 탭과도 맞지 않아 내용이 통째로 비어 버린다.
  const pathname = withoutTrailingSlash(useLocation().pathname);
  const navigate = useNavigate();

  const alarmTabs = [
    { id: 'alarm', text: 'Alarms', path: APP_PATH.CONFIG_ALARM },
    { id: 'webhook', text: 'Webhook', path: APP_PATH.CONFIG_WEBHOOK },
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
