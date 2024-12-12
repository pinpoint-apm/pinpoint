import React from 'react';
import { useTranslation } from 'react-i18next';
import { LayoutWithAlarm } from '../../components/Layout/LayoutWithAlarm';
import {
  ApplicationCombinedList,
  ApplicationCombinedListProps,
  Button,
  Separator,
  useReactToastifyToast,
} from '../../components';
import { APP_SETTING_KEYS, ApplicationType, Webhook } from '@pinpoint-fe/constants';
import { cn } from '../../lib/utils';
import { WebhookList } from '../../components/Webhook/WebhookList';
import { WebhookTable } from '../../components/Webhook/WebhookTable';
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
import { Cross2Icon } from '@radix-ui/react-icons';
import { ScrollArea } from '../../components/ui/scroll-area';
import { WebhookDetail } from '../../components/Webhook/WebhookDetail';
import { useGetWebhook, useLocalStorage, useWebhookMutation } from '@pinpoint-fe/ui/hooks';
import { MdOutlineAdd } from 'react-icons/md';

export interface WebhookPageProps {
  ApplicationList?: (props: ApplicationCombinedListProps) => JSX.Element;
}

export const WebhookPage = ({ ApplicationList = ApplicationCombinedList }: WebhookPageProps) => {
  const toast = useReactToastifyToast();
  const { t } = useTranslation();
  const [selectedApplication, setSelectedApplication] = useLocalStorage<
    ApplicationType | undefined
  >(APP_SETTING_KEYS.CONFIG_LAST_SELECTED_APPLICATION, undefined);
  const [isEditable, setEditable] = React.useState(false);
  const [currentDeletingTarget, setCurrentDeletingTarget] = React.useState<Webhook.WebhookData>();
  const [currentTargetWebhookData, setCurrentTargetWebhookData] =
    React.useState<Partial<Webhook.WebhookData>>();
  const { refetch: refetchWebhookList } = useGetWebhook({
    applicationId: selectedApplication?.applicationName,
  });

  const { mutate } = useWebhookMutation({
    onSuccess: () => {
      toast.success(t('COMMON.REMOVE_SUCCESS'));
      setCurrentDeletingTarget(undefined);
      setCurrentTargetWebhookData(undefined);
      refetchWebhookList();
    },
    onError: () => {
      toast.success(t('COMMON.REMOVE_FAIL'));
    },
  });

  return (
    <LayoutWithAlarm>
      <div className="space-y-3">
        <div className="flex gap-2">
          <ApplicationList
            triggerClassName={cn(
              'flex items-center px-2 pb-2 pt-1 border rounded-md shadow-sm h-9 border-input',
            )}
            contentClassName="PopoverContent"
            open={false}
            selectedApplication={selectedApplication}
            onClickApplication={(app) => setSelectedApplication(app)}
          />
          <Button
            disabled={!selectedApplication}
            onClick={() => {
              setEditable(true);
              setCurrentTargetWebhookData({
                applicationId: selectedApplication?.applicationName,
              });
            }}
          >
            <MdOutlineAdd className="mr-1" />
            {t('CONFIGURATION.WEBHOOK.CREATE')}
          </Button>
        </div>
        <WebhookList applicationId={selectedApplication?.applicationName}>
          {(webhookList) => (
            <WebhookTable
              data={webhookList}
              onClickRowItem={(data) => {
                setEditable(false);
                setCurrentTargetWebhookData(data);
              }}
              onClickEdit={(data) => {
                setEditable(true);
                setCurrentTargetWebhookData(data);
              }}
              onClickDelete={(data) => setCurrentDeletingTarget(data)}
            />
          )}
        </WebhookList>
      </div>
      <Sheet
        open={!!currentTargetWebhookData}
        onOpenChange={(open) => {
          !open && setCurrentTargetWebhookData(undefined);
        }}
      >
        <SheetContent
          hideClose
          className="flex flex-col w-full gap-0 p-0 px-0 md:max-w-full md:w-2/5"
        >
          <SheetHeader className="px-4 bg-secondary/50">
            <SheetTitle className="relative flex items-center justify-between h-24 gap-1 pt-8 pb-6">
              {isEditable
                ? currentTargetWebhookData?.webhookId
                  ? t('CONFIGURATION.WEBHOOK.EDIT')
                  : t('CONFIGURATION.WEBHOOK.CREATE')
                : t('CONFIGURATION.WEBHOOK.DETAIL')}
              <div className="flex gap-1">
                {isEditable ? null : (
                  <>
                    <Button
                      variant="outline"
                      className="py-1 text-destructive hover:text-destructive"
                      onClick={() =>
                        setCurrentDeletingTarget(currentTargetWebhookData as Webhook.WebhookData)
                      }
                    >
                      {t('COMMON.DELETE')}
                    </Button>
                    <Button variant="outline" className="py-1" onClick={() => setEditable(true)}>
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
              <WebhookDetail
                editable={isEditable}
                data={currentTargetWebhookData}
                onClickCancel={() => {
                  setEditable(false);
                }}
                onCompleteMutation={() => {
                  setCurrentTargetWebhookData(undefined);
                  refetchWebhookList();
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
                target: `${currentDeletingTarget?.alias} (${currentDeletingTarget?.url})`,
              })}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {t('COMMON.DELETE_CONFIRM_DESCRIPTION', { target: t('CONFIGURATION.WEBHOOK.NAME') })}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>{t('COMMON.CANCEL')}</AlertDialogCancel>
            <AlertDialogAction
              buttonVariant={{ variant: 'destructive' }}
              onClick={() => {
                if (currentDeletingTarget) {
                  mutate({
                    params: {
                      alias: currentDeletingTarget.alias,
                      applicationId: currentDeletingTarget.applicationId,
                      serviceName: currentDeletingTarget.serviceName,
                      url: currentDeletingTarget.url,
                      webhookId: currentDeletingTarget.webhookId,
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
