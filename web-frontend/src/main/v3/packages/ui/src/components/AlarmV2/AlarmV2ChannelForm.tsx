import React from 'react';
import { useTranslation } from 'react-i18next';
import { TFunction } from 'i18next';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { AlarmV2Channel } from '@pinpoint-fe/ui/src/constants/types';
import { ConfigUserGroup } from '@pinpoint-fe/ui/src/constants';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Input,
  Button,
} from '@pinpoint-fe/ui/src/components/ui';
import { LoadingButton } from '@pinpoint-fe/ui';
import { cn } from '@pinpoint-fe/ui/src/lib/utils';
import { MdOutlineWarningAmber } from 'react-icons/md';
import { hasNoRecipient } from './hasNoRecipient';

type WebhookFormat = 'DEFAULT' | 'SLACK';

const makeFormSchema = (t: TFunction) =>
  z
    .object({
      channelName: z.string().min(1, t('CONFIGURATION.ALARM_V2.VALIDATION_NAME_REQUIRED')),
      methodType: z.enum(['SMS', 'EMAIL', 'WEBHOOK']),
      destination: z.string().min(1, t('CONFIGURATION.ALARM_V2.VALIDATION_DESTINATION_REQUIRED')),
      webhookFormat: z.enum(['DEFAULT', 'SLACK']).optional(),
    })
    .superRefine((data, ctx) => {
      if (data.methodType === 'WEBHOOK') {
        if (!isHttpUrl(data.destination)) {
          ctx.addIssue({
            code: z.ZodIssueCode.custom,
            message: t('CONFIGURATION.ALARM_V2.VALIDATION_INVALID_URL'),
            path: ['destination'],
          });
        }
      }
    });

type FormValues = z.infer<ReturnType<typeof makeFormSchema>>;

interface ParsedChannelConfig {
  format: WebhookFormat;
  values?: AlarmV2Channel.Config;
}

const parseChannelConfig = (config?: AlarmV2Channel.Config | null): ParsedChannelConfig => {
  if (!config) return { format: 'DEFAULT' };
  if (typeof config !== 'object' || Array.isArray(config)) {
    return { format: 'DEFAULT' };
  }
  return {
    format:
      typeof config.format === 'string' && config.format.toUpperCase() === 'SLACK'
        ? 'SLACK'
        : 'DEFAULT',
    values: config,
  };
};

const isHttpUrl = (value: string) => {
  try {
    const { protocol } = new URL(value);
    return protocol === 'http:' || protocol === 'https:';
  } catch {
    return false;
  }
};

export interface AlarmV2ChannelFormProps {
  data?: Partial<AlarmV2Channel.ChannelData>;
  userGroups?: ConfigUserGroup.UserGroup[];
  pending?: boolean;
  controlsDisabled?: boolean;
  controlsDisabledReason?: string;
  onSubmit: (values: AlarmV2Channel.ChannelWriteData) => void;
  onCancel?: () => void;
}

export const AlarmV2ChannelForm = ({
  data,
  userGroups,
  pending,
  controlsDisabled,
  controlsDisabledReason,
  onSubmit,
  onCancel,
}: AlarmV2ChannelFormProps) => {
  const { t } = useTranslation();
  const isEdit = !!data?.id;
  const formSchema = React.useMemo(() => makeFormSchema(t), [t]);
  const parsedConfig = React.useMemo(() => parseChannelConfig(data?.config), [data?.config]);
  const defaultValues = React.useMemo<FormValues>(
    () => ({
      channelName: data?.channelName || '',
      methodType: data?.methodType || 'EMAIL',
      destination: data?.destination || '',
      webhookFormat: parsedConfig.format,
    }),
    [data?.channelName, data?.methodType, data?.destination, parsedConfig.format],
  );

  const form = useForm<FormValues>({
    resolver: zodResolver(formSchema),
    defaultValues,
  });

  const watchMethodType = form.watch('methodType');
  const formControlsDisabled = !!controlsDisabled || !!pending;
  // The count the server sent describes the group the channel was saved with. Once the user
  // picks another group it says nothing about the one on screen, so it goes away until saved.
  const showNoRecipient =
    hasNoRecipient(data) &&
    form.watch('destination') === data?.destination &&
    form.watch('methodType') === data?.methodType;

  React.useEffect(() => {
    form.reset(defaultValues);
  }, [defaultValues, form]);

  const handleSubmit = (values: FormValues) => {
    if (formControlsDisabled) return;

    const { webhookFormat, ...rest } = values;
    // The current form edits only the webhook format. Preserve message templates and
    // future config fields that may have been written through the API.
    let config = data?.config;
    if (values.methodType === 'WEBHOOK' && webhookFormat !== parsedConfig.format) {
      const nextConfig = { ...parsedConfig.values };
      if (webhookFormat === 'SLACK') {
        nextConfig.format = 'SLACK';
      } else {
        delete nextConfig.format;
      }
      config = Object.keys(nextConfig).length > 0 ? nextConfig : null;
    } else if (
      values.methodType !== 'WEBHOOK' &&
      parsedConfig.values &&
      Object.prototype.hasOwnProperty.call(parsedConfig.values, 'format')
    ) {
      const nextConfig = { ...parsedConfig.values };
      delete nextConfig.format;
      config = Object.keys(nextConfig).length > 0 ? nextConfig : null;
    }
    onSubmit({ ...rest, ...(config !== undefined ? { config } : {}) });
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)} className="space-y-4">
        <FormField
          control={form.control}
          name="channelName"
          render={({ field, fieldState }) => (
            <FormItem>
              <FormLabel>{t('CONFIGURATION.COMMON.NAME')}</FormLabel>
              <FormControl>
                <Input
                  {...field}
                  className={cn({ 'border-destructive': fieldState.invalid })}
                  disabled={formControlsDisabled}
                  placeholder={t('CONFIGURATION.ALARM_V2.CHANNEL_NAME_PLACEHOLDER')}
                />
              </FormControl>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="methodType"
          render={({ field }) => (
            <FormItem>
              <FormLabel>{t('CONFIGURATION.COMMON.TYPE')}</FormLabel>
              <Select
                onValueChange={(methodType) => {
                  const nextMethodType = methodType as FormValues['methodType'];
                  const destinationTypeChanged =
                    (field.value === 'WEBHOOK') !== (nextMethodType === 'WEBHOOK');
                  if (!isEdit || destinationTypeChanged) {
                    form.setValue('destination', '');
                  }
                  if (!isEdit) {
                    form.setValue('webhookFormat', 'DEFAULT');
                  }
                  field.onChange(nextMethodType);
                }}
                value={field.value}
                disabled={formControlsDisabled}
              >
                <FormControl>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                </FormControl>
                <SelectContent>
                  <SelectItem value="EMAIL">EMAIL</SelectItem>
                  <SelectItem value="SMS">SMS</SelectItem>
                  <SelectItem value="WEBHOOK">WEBHOOK</SelectItem>
                </SelectContent>
              </Select>
              <FormMessage />
            </FormItem>
          )}
        />

        <FormField
          control={form.control}
          name="destination"
          render={({ field, fieldState }) => (
            <FormItem>
              <FormLabel>
                {watchMethodType === 'WEBHOOK'
                  ? t('CONFIGURATION.ALARM_V2.WEBHOOK_LABEL')
                  : t('CONFIGURATION.ALARM_V2.USER_GROUP_LABEL')}
              </FormLabel>
              {watchMethodType === 'WEBHOOK' ? (
                <FormControl>
                  <Input
                    {...field}
                    className={cn({ 'border-destructive': fieldState.invalid })}
                    disabled={formControlsDisabled}
                    placeholder={t('CONFIGURATION.ALARM_V2.WEBHOOK_URL_PLACEHOLDER')}
                  />
                </FormControl>
              ) : (
                <Select
                  onValueChange={field.onChange}
                  value={field.value}
                  disabled={formControlsDisabled}
                >
                  <FormControl>
                    <SelectTrigger className={cn({ 'border-destructive': fieldState.invalid })}>
                      <SelectValue placeholder={t('CONFIGURATION.ALARM_V2.SELECT_USER_GROUP')} />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    {userGroups?.map((ug) => (
                      <SelectItem key={ug.id} value={ug.id || ''}>
                        {ug.id}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
              {showNoRecipient && (
                <FormDescription className="flex items-start gap-1.5 text-sm text-orange-700">
                  <MdOutlineWarningAmber className="mt-0.5 size-4 shrink-0" aria-hidden="true" />
                  {t('CONFIGURATION.ALARM_V2.NO_RECIPIENT_DESCRIPTION')}
                </FormDescription>
              )}
              <FormMessage />
            </FormItem>
          )}
        />

        {watchMethodType === 'WEBHOOK' && (
          <FormField
            control={form.control}
            name="webhookFormat"
            render={({ field }) => (
              <FormItem>
                <FormLabel>{t('CONFIGURATION.ALARM_V2.WEBHOOK_FORMAT')}</FormLabel>
                <Select
                  onValueChange={field.onChange}
                  value={field.value || 'DEFAULT'}
                  disabled={formControlsDisabled}
                >
                  <FormControl>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                  </FormControl>
                  <SelectContent>
                    <SelectItem value="DEFAULT">
                      {t('CONFIGURATION.ALARM_V2.WEBHOOK_FORMAT_DEFAULT')}
                    </SelectItem>
                    <SelectItem value="SLACK">
                      {t('CONFIGURATION.ALARM_V2.WEBHOOK_FORMAT_SLACK')}
                    </SelectItem>
                  </SelectContent>
                </Select>
              </FormItem>
            )}
          />
        )}

        <div className="space-y-2 pt-2">
          {controlsDisabledReason && (
            <p className="text-xs text-muted-foreground">{controlsDisabledReason}</p>
          )}
          <div className="flex justify-end gap-2">
            {onCancel && (
              <Button type="button" variant="outline" onClick={onCancel} disabled={pending}>
                {t('COMMON.CANCEL')}
              </Button>
            )}
            <LoadingButton type="submit" pending={pending} disabled={formControlsDisabled}>
              {isEdit ? t('COMMON.SAVE') : t('COMMON.ADD')}
            </LoadingButton>
          </div>
        </div>
      </form>
    </Form>
  );
};
