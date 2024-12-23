import { APP_PATH } from '@pinpoint-fe/constants';
import { Separator } from '../../components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../../components/ui/tabs';
import { t } from 'i18next';
import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { configurationAtom } from '@pinpoint-fe/ui/atoms';
import { useAtomValue } from 'jotai';

export interface LayoutWithAlarmProps {
  children?: React.ReactNode;
}

export const LayoutWithAlarm = ({ children }: LayoutWithAlarmProps) => {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const configuration = useAtomValue(configurationAtom);

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
