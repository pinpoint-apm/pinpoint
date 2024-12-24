import React from 'react';
import { useTranslation } from 'react-i18next';
import {
  APP_SETTING_KEYS,
  AlarmRule,
  ApplicationType,
  Configuration,
} from '@pinpoint-fe/ui/constants';
import { cn } from '../../lib';
import { Separator } from '../../components/ui/separator';
import {
  ApplicationCombinedList,
  ApplicationCombinedListProps,
} from '../../components/Application/ApplicationCombinedList';
import { AlarmList } from '../../components/Alarm/AlarmList';
import { AlarmDetail } from '../../components/Alarm/AlarmDetail';
import {
  Sheet,
  SheetClose,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '../../components/ui/sheet';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '../../components/ui/alert-dialog';
import { Button } from '../../components/ui/button';
import { useReactToastifyToast } from '../../components/Toast';
import { Cross2Icon } from '@radix-ui/react-icons';
import { ScrollArea } from '../../components/ui/scroll-area';
import { useAlarmRuleMutation, useAlarmRuleQuery, useLocalStorage } from '@pinpoint-fe/ui/hooks';
import { LayoutWithAlarm } from '../../components/Layout/LayoutWithAlarm';
import { useSetAtom } from 'jotai';
import { configurationAtom } from '@pinpoint-fe/ui/atoms';
import { AlarmPermissionContext } from '../../components';
import { MdOutlineAdd } from 'react-icons/md';

export interface AlarmPageProps {
  configuration?: Configuration;
  ApplicationList?: (props: ApplicationCombinedListProps) => JSX.Element;
  onChangeApplication?: (application: ApplicationType) => void;
}

export const AlarmPage = ({
  ApplicationList = ApplicationCombinedList,
  configuration,
  onChangeApplication,
}: AlarmPageProps) => {
  const toast = useReactToastifyToast();
  const setConfigurationAtom = useSetAtom(configurationAtom);
  const { t } = useTranslation();
  const [selectedApplication, setSelectedApplication] = useLocalStorage<
    ApplicationType | undefined
  >(APP_SETTING_KEYS.CONFIG_LAST_SELECTED_APPLICATION, undefined);
  const [isEditable, setEditable] = React.useState(false);
  const [currentDeletingTarget, setCurrentDeletingTarget] =
    React.useState<AlarmRule.AlarmRuleData>();
  const [currentTargetAlarmData, setCurrentTargetAlarmData] =
    React.useState<Partial<AlarmRule.AlarmRuleData>>();
  const { refetch: refetchAlarmList } = useAlarmRuleQuery({
    applicationId: selectedApplication?.applicationName,
  });
  const { mutate } = useAlarmRuleMutation({
    onSuccess: () => {
      toast.success(t('COMMON.REMOVE_SUCCESS'));
      setCurrentDeletingTarget(undefined);
      setCurrentTargetAlarmData(undefined);
      refetchAlarmList();
    },
    onError: () => {
      toast.error(t('COMMON.REMOVE_FAIL'));
    },
  });
  const { permissionContext } = React.useContext(AlarmPermissionContext);

  React.useEffect(() => {
    if (configuration) {
      setConfigurationAtom(configuration);
    }
  }, [configuration]);

  React.useEffect(() => {
    if (selectedApplication) {
      onChangeApplication?.(selectedApplication);
    }
  }, [selectedApplication]);

  return (
    <LayoutWithAlarm>
      <div className="space-y-3">
        <div className="flex gap-2">
          <ApplicationList
            open={false}
            triggerClassName={cn(
              'flex items-center px-2 pb-2 pt-1 border rounded-md shadow-sm h-9 border-input',
            )}
            contentClassName="PopoverContent"
            selectedApplication={selectedApplication}
            onClickApplication={(app) => setSelectedApplication(app)}
          />
          <Button
            disabled={!selectedApplication || !permissionContext.create}
            onClick={() => {
              setEditable(true);
              setCurrentTargetAlarmData({
                serviceType: selectedApplication?.serviceType,
                applicationId: selectedApplication?.applicationName,
                smsSend: true,
                emailSend: true,
                webhookSend: true,
              });
            }}
          >
            <MdOutlineAdd className="mr-1" />
            {t('CONFIGURATION.ALARM.ADD')}
          </Button>
        </div>
        <AlarmList
          applicationId={selectedApplication?.applicationName}
          onClickRowItem={(data) => {
            setEditable(false);
            setCurrentTargetAlarmData(data);
          }}
          onClickEdit={(data) => {
            setEditable(true);
            setCurrentTargetAlarmData(data);
          }}
          onClickDelete={(data) => {
            setCurrentDeletingTarget(data);
          }}
        />
      </div>
      <Sheet
        open={!!currentTargetAlarmData}
        onOpenChange={(open) => {
          !open && setCurrentTargetAlarmData(undefined);
        }}
      >
        <SheetContent
          hideClose
          className="flex flex-col w-full gap-0 p-0 px-0 md:max-w-full md:w-2/5"
        >
          <SheetHeader className="px-4 bg-secondary/50">
            <SheetTitle className="relative flex items-center justify-between h-24 gap-1 pt-8 pb-6">
              {isEditable
                ? currentTargetAlarmData?.ruleId
                  ? t('CONFIGURATION.ALARM.EDIT')
                  : t('CONFIGURATION.ALARM.ADD')
                : t('CONFIGURATION.ALARM.DETAIL')}
              <div className="flex gap-1">
                {isEditable ? null : (
                  <>
                    <Button
                      variant="outline"
                      className="py-1 text-destructive hover:text-destructive"
                      disabled={!permissionContext.delete}
                      onClick={() =>
                        setCurrentDeletingTarget(currentTargetAlarmData as AlarmRule.AlarmRuleData)
                      }
                    >
                      {t('COMMON.DELETE')}
                    </Button>
                    <Button
                      variant="outline"
                      className="py-1"
                      onClick={() => setEditable(true)}
                      disabled={!permissionContext.edit}
                    >
                      {t('COMMON.EDIT')}
                    </Button>
                  </>
                )}
              </div>
              <SheetClose
                className={cn({
                  'absolute left-0 top-1 text-muted-foreground': !isEditable,
                })}
              >
                <Cross2Icon className="w-4 h-4" />
              </SheetClose>
            </SheetTitle>
          </SheetHeader>
          <Separator />
          <ScrollArea>
            <div className="p-4">
              <AlarmDetail
                editable={isEditable}
                data={currentTargetAlarmData}
                onClickCancel={() => setEditable(false)}
                onCompleteMutation={() => {
                  setCurrentTargetAlarmData(undefined);
                  refetchAlarmList();
                }}
              />
            </div>
          </ScrollArea>
        </SheetContent>
      </Sheet>
      <AlertDialog
        open={!!currentDeletingTarget}
        onOpenChange={(open) => {
          !open && setCurrentDeletingTarget(undefined);
        }}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {t('COMMON.DELETE_CONFIRM_QUESTION', {
                target: `${currentDeletingTarget?.checkerName} (${currentDeletingTarget?.userGroupId})`,
              })}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {t('COMMON.DELETE_CONFIRM_DESCRIPTION', { target: t('CONFIGURATION.ALARM.NAME') })}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>{t('COMMON.CANCEL')}</AlertDialogCancel>
            <AlertDialogAction
              disabled={!permissionContext.delete}
              buttonVariant={{ variant: 'destructive' }}
              onClick={() => {
                if (currentDeletingTarget) {
                  mutate({
                    params: {
                      applicationId: currentDeletingTarget?.applicationId,
                      ruleId: currentDeletingTarget?.ruleId,
                      emailSend: currentDeletingTarget?.emailSend,
                      smsSend: currentDeletingTarget?.smsSend,
                      webhookSend: currentDeletingTarget?.webhookSend,
                    },
                    method: 'DELETE',
                  });
                }
              }}
            >
              {t('COMMON.CONTINUE')}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </LayoutWithAlarm>
  );
};
