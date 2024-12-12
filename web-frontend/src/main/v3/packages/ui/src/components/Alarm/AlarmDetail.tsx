import React from 'react';
import { useTranslation } from 'react-i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { AlarmRule, ErrorResponse } from '@pinpoint-fe/constants';
import omit from 'lodash.omit';
import {
  useAlarmRuleMutation,
  useGetAlarmRuleChecker,
  useGetUserGroup,
  useGetWebhook,
  useWebhookIncludeMutaion,
} from '@pinpoint-fe/ui/hooks';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '../../components/ui/form';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../../components/ui/select';
import { Input } from '../../components/ui/input';
import { Textarea } from '../../components/ui/textarea';
import { Optional } from '../../components/Form/Optional';
import { WebhookCheckedList } from '../../components/Webhook/WebhookCheckedList';
import { Button } from '../../components/ui/button';
import { useReactToastifyToast } from '../../components/Toast';
import { ErrorToast } from '../../components/Error/ErrorToast';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '../../components/ui/dialog';
import { WebhookDetail } from '../../components/Webhook/WebhookDetail';
import { cn } from '../../lib/utils';
import { configurationAtom } from '@pinpoint-fe/atoms';
import { useAtomValue } from 'jotai';
import { HelpPopover } from '../../components/HelpPopover';

export interface AlarmDetailProps {
  data?: Partial<AlarmRule.AlarmRuleData>;
  editable?: boolean;
  onClickCancel?: () => void;
  onCompleteMutation?: () => void;
}

type AlarmTypeKeys =
  | keyof Pick<AlarmRule.AlarmRuleData, 'smsSend' | 'emailSend' | 'webhookSend'>
  | 'all'
  | 'none';

const getAlarmTypeSelectList = (webhookEnable: boolean) => {
  const defaultList = [
    { key: 'all', text: 'All' },
    { key: 'emailSend', text: 'Email' },
    { key: 'smsSend', text: 'SMS' },
    { key: 'none', text: 'None' },
  ];
  if (webhookEnable) defaultList.push({ key: 'webhookSend', text: 'Webhook' });
  return defaultList;
};

const getAlarmType = (data?: Partial<AlarmRule.AlarmRuleData>): AlarmTypeKeys => {
  if (data) {
    if (data.smsSend && data.emailSend && data.webhookSend) {
      return 'all';
    } else if (data.smsSend) {
      return 'smsSend';
    } else if (data.emailSend) {
      return 'emailSend';
    } else if (data.webhookSend) {
      return 'webhookSend';
    } else {
      return 'none';
    }
  } else {
    return 'all';
  }
};

const parseAlarmType = (
  type: AlarmTypeKeys,
): Pick<AlarmRule.AlarmRuleData, 'smsSend' | 'emailSend' | 'webhookSend'> => {
  const defaultType = {
    smsSend: false,
    emailSend: false,
    webhookSend: false,
  };
  if (type === 'all') {
    return {
      smsSend: true,
      emailSend: true,
      webhookSend: true,
    };
  } else if (type === 'smsSend') {
    return {
      ...defaultType,
      smsSend: true,
    };
  } else if (type === 'emailSend') {
    return {
      ...defaultType,
      emailSend: true,
    };
  } else if (type === 'webhookSend') {
    return {
      ...defaultType,
      webhookSend: true,
    };
  } else {
    return defaultType;
  }
};

const formSchema = z.object({
  checkerName: z.string({ required_error: 'Select Checker' }),
  userGroupId: z.string({ required_error: 'Select User Group ID' }),
  threshold: z
    .number()
    .min(0, { message: 'Must be greater than 0' })
    .max(2147483647, { message: 'Must be less than 2147483647' }),
  type: z.string(),
  webhook: z.string().array().optional(),
  notes: z.string().optional(),
});

export const AlarmDetail = ({
  data,
  editable,
  onClickCancel,
  onCompleteMutation,
}: AlarmDetailProps) => {
  const toast = useReactToastifyToast();
  const { t } = useTranslation();
  const configuration = useAtomValue(configurationAtom);
  const [openWebhookDialog, setOpenWebhookDialog] = React.useState(false);
  const { data: chekerList } = useGetAlarmRuleChecker({ disableFetch: !editable });
  const { refetch: refetchWebhookList } = useGetWebhook({
    applicationId: data?.applicationId,
  });
  const { refetch: refetchUsedWebhookList } = useGetWebhook({
    ruleId: data?.ruleId,
  });
  const { data: userGroupList } = useGetUserGroup({ disableFetch: !editable });
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      notes: data?.notes || '',
      checkerName: data?.checkerName,
      userGroupId: data?.userGroupId,
      threshold: data?.threshold || 1,
      type: getAlarmType(data),
    },
  });
  const watchType = form.watch('type');
  const enableWebhook =
    (watchType === 'webhookSend' || watchType === 'all') && configuration?.webhookEnable;
  const handleMutationSuccess = () => {
    toast.success(data?.ruleId ? t('COMMON.UPDATE_SUCCESS') : t('COMMON.CREATE_SUCCESS'));
    refetchUsedWebhookList();
    onCompleteMutation?.();
  };
  const handleMutationError = (error: ErrorResponse) => {
    toast.error(<ErrorToast error={error} />, {
      className: 'pointer-events-auto',
      bodyClassName: '!items-start',
      autoClose: false,
    });
  };
  const { mutate: alarmRuleMutate } = useAlarmRuleMutation({
    onSuccess: handleMutationSuccess,
    onError: handleMutationError,
  });
  const { mutate: includeWebhookMutate } = useWebhookIncludeMutaion({
    onSuccess: handleMutationSuccess,
    onError: handleMutationError,
  });

  const handleSubmit = async (value: z.infer<typeof formSchema>) => {
    const parsedAlarmType = parseAlarmType(value.type as AlarmTypeKeys);
    const method = data?.ruleId ? 'PUT' : 'POST';
    const mergedAlarm = omit(
      {
        ...data,
        ...value,
        ...parsedAlarmType,
      },
      ['type', 'webhook'],
    ) as AlarmRule.AlarmRuleData;

    if (parsedAlarmType.webhookSend && value.webhook) {
      // include Webhook
      includeWebhookMutate({
        params: {
          rule: mergedAlarm,
          webhookIds: value.webhook,
        },
        method,
      });
    } else {
      alarmRuleMutate({
        params: mergedAlarm,
        method,
      });
    }
  };

  return (
    <>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(handleSubmit)}>
          <div className="grid gap-4">
            <FormField
              control={form.control}
              name="checkerName"
              render={({ field, fieldState }) => {
                return (
                  <FormItem>
                    <FormLabel className="flex items-center gap-1">
                      {t('CONFIGURATION.COMMON.CHECKER')}
                      <HelpPopover helpKey="HELP_VIEWER.ALARM" />
                    </FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                      disabled={!editable}
                    >
                      <FormControl>
                        <>
                          {editable ? (
                            <SelectTrigger
                              className={cn('w-90', { 'border-destructive': fieldState.invalid })}
                            >
                              <SelectValue placeholder="Select rule" />
                            </SelectTrigger>
                          ) : (
                            <Input className="w-90" value={field.value} disabled />
                          )}
                        </>
                      </FormControl>
                      <SelectContent className="z-[51]">
                        {chekerList?.map((checker) => (
                          <SelectItem key={checker} value={checker}>
                            {checker}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormDescription></FormDescription>
                    <FormMessage />
                  </FormItem>
                );
              }}
            />
            <FormField
              control={form.control}
              name="userGroupId"
              render={({ field, fieldState }) => {
                return (
                  <FormItem>
                    <FormLabel>{t('CONFIGURATION.COMMON.USER_GROUP')}</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                      disabled={!editable}
                    >
                      <FormControl>
                        <>
                          {' '}
                          {editable ? (
                            <SelectTrigger
                              className={cn('w-90', { 'border-destructive': fieldState.invalid })}
                            >
                              <SelectValue placeholder="Select user group" />
                            </SelectTrigger>
                          ) : (
                            <Input className="w-90" value={field.value} disabled />
                          )}
                        </>
                      </FormControl>
                      <SelectContent>
                        {userGroupList?.map((item) => (
                          <SelectItem key={item.id} value={item.id}>
                            {item.id}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormDescription></FormDescription>
                    <FormMessage />
                  </FormItem>
                );
              }}
            />
            <FormField
              control={form.control}
              name="threshold"
              render={({ field, fieldState }) => {
                return (
                  <FormItem>
                    <FormLabel>{t('CONFIGURATION.COMMON.THRESHOLD')}</FormLabel>
                    <FormControl>
                      <Input
                        type="number"
                        className={cn('w-40', { 'border-destructive': fieldState.invalid })}
                        {...field}
                        value={field.value}
                        disabled={!editable}
                        onChange={(value) => field.onChange(value.target.valueAsNumber)}
                      />
                    </FormControl>
                    <FormDescription></FormDescription>
                    <FormMessage />
                  </FormItem>
                );
              }}
            />
            <FormField
              control={form.control}
              name="type"
              render={({ field }) => {
                return (
                  <FormItem>
                    <FormLabel>
                      <Optional hide={!editable}>{t('CONFIGURATION.COMMON.TYPE')}</Optional>
                    </FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue={field.value}
                      disabled={!editable}
                    >
                      <FormControl>
                        <SelectTrigger className="w-40">
                          <SelectValue placeholder="Select a verified email to display" />
                        </SelectTrigger>
                      </FormControl>
                      <SelectContent>
                        {getAlarmTypeSelectList(!!configuration?.webhookEnable).map((type) => (
                          <SelectItem key={type.key} value={type.key}>
                            {type.text}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <FormDescription></FormDescription>
                    <FormMessage />
                  </FormItem>
                );
              }}
            />
            {enableWebhook && (
              <FormField
                control={form.control}
                name="webhook"
                render={() => {
                  return (
                    <FormItem>
                      <FormLabel>
                        <Optional hide={!editable}>{t('CONFIGURATION.COMMON.WEBHOOK')}</Optional>
                        <Button
                          variant="link"
                          className="float-right px-2 py-1 text-xs h-7"
                          disabled={!editable}
                          onClick={(e) => {
                            e.preventDefault();
                            setOpenWebhookDialog(true);
                          }}
                        >
                          {t('CONFIGURATION.WEBHOOK.CREATE')}
                        </Button>
                      </FormLabel>
                      <FormControl>
                        <WebhookCheckedList
                          disabled={!editable}
                          disableFetch={!enableWebhook}
                          onCheckedChange={(checkedList) => {
                            form.setValue('webhook', checkedList);
                          }}
                          {...data}
                        />
                      </FormControl>
                      <FormDescription></FormDescription>
                      <FormMessage />
                    </FormItem>
                  );
                }}
              />
            )}
            <FormField
              control={form.control}
              name="notes"
              render={({ field }) => {
                return (
                  <FormItem>
                    <FormLabel>
                      <Optional hide={!editable}>{t('CONFIGURATION.COMMON.NOTES')}</Optional>
                    </FormLabel>
                    <FormControl>
                      <Textarea {...field} maxLength={300} disabled={!editable} />
                    </FormControl>
                    <FormDescription></FormDescription>
                    <FormMessage />
                  </FormItem>
                );
              }}
            />
            {editable && (
              <div className="flex justify-end gap-2">
                {data?.ruleId && (
                  <Button variant="outline" onClick={() => onClickCancel?.()}>
                    {t('COMMON.CANCEL')}
                  </Button>
                )}
                <Button type="submit">{data?.ruleId ? t('COMMON.SAVE') : t('COMMON.ADD')}</Button>
              </div>
            )}
          </div>
        </form>
      </Form>
      <Dialog open={openWebhookDialog} onOpenChange={setOpenWebhookDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t('CONFIGURATION.WEBHOOK.CREATE')}</DialogTitle>
          </DialogHeader>
          <WebhookDetail
            editable
            data={{
              applicationId: data?.applicationId,
            }}
            onClickCancel={() => {
              setOpenWebhookDialog(false);
            }}
            onCompleteMutation={() => {
              setOpenWebhookDialog(false);
              refetchWebhookList();
            }}
          />
        </DialogContent>
      </Dialog>
    </>
  );
};
