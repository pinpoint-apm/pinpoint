import { useForm } from 'react-hook-form';
import * as z from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { Webhook } from '@pinpoint-fe/constants';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '../../components/ui/form';
import { Input, Button } from '../../components';
import { useReactToastifyToast } from '../../components/Toast';
import { Optional } from '../../components/Form/Optional';
import { useWebhookMutation } from '@pinpoint-fe/ui/hooks';
import { ErrorToast } from '../../components/Error/ErrorToast';
import { useTranslation } from 'react-i18next';
import { cn } from '../../lib/utils';

export interface WebhookDetailProps {
  data?: Partial<Webhook.WebhookData>;
  editable?: boolean;
  onClickCancel?: () => void;
  onCompleteMutation?: () => void;
}

const formSchema = z.object({
  alias: z.string().max(256).optional(),
  url: z
    .string({ required_error: 'Url is required' })
    .max(256)
    .regex(
      new RegExp(
        // eslint-disable-next-line
        /((([A-Za-z]{3,9}:(?:\/\/)?)(?:[\-;:&=\+\$,\w]+@)?[A-Za-z0-9\.\-]+|(?:www\.|[\-;:&=\+\$,\w]+@)[A-Za-z0-9\.\-]+)((?:\/[\+~%\/\.\w\-_]*)?\??(?:[\-\+=&;%@\.\w_]*)#?(?:[\.\!\/\\\w]*))?)/,
      ),
      'Invalied Url format',
    ),
});

export const WebhookDetail = ({
  data,
  editable,
  onClickCancel,
  onCompleteMutation,
}: WebhookDetailProps) => {
  const toast = useReactToastifyToast();
  const { t } = useTranslation();
  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      alias: data?.alias,
      url: data?.url,
    },
  });
  const { mutate: webhookMutate } = useWebhookMutation({
    onSuccess: () => {
      toast.success(data?.webhookId ? t('COMMON.UPDATE_SUCCESS') : t('COMMON.CREATE_SUCCESS'));
      onCompleteMutation?.();
    },
    onError: (error) => {
      toast.error(<ErrorToast error={error} />, {
        className: 'pointer-events-auto',
        bodyClassName: '!items-start',
        autoClose: false,
      });
    },
  });

  const handleSubmit = async (value: z.infer<typeof formSchema>) => {
    const method = data?.webhookId ? 'PUT' : 'POST';
    const mergedWebhook = {
      ...data,
      ...value,
      alias: value.alias ? value.alias : value.url,
    } as Webhook.WebhookData;
    webhookMutate({ params: mergedWebhook, method });
  };

  return (
    <Form {...form}>
      <form onSubmit={form.handleSubmit(handleSubmit)}>
        <div className="grid gap-4">
          <FormField
            control={form.control}
            name="alias"
            render={({ field }) => {
              return (
                <FormItem>
                  <FormLabel>
                    <Optional hide={!editable}>{t('CONFIGURATION.WEBHOOK.ALIAS')}</Optional>
                  </FormLabel>
                  <FormControl>
                    <Input
                      {...field}
                      disabled={!editable}
                      placeholder="If empty, it is set to the entered Url"
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
            name="url"
            render={({ field, fieldState }) => {
              return (
                <FormItem>
                  <FormLabel>Url</FormLabel>
                  <FormControl>
                    <Input
                      {...field}
                      className={cn({ 'border-destructive': fieldState.invalid })}
                      disabled={!editable}
                    />
                  </FormControl>
                  <FormDescription></FormDescription>
                  <FormMessage />
                </FormItem>
              );
            }}
          />
          {editable && (
            <div className="flex justify-end gap-2">
              {data?.webhookId && (
                <Button variant="outline" onClick={() => onClickCancel?.()}>
                  {t('COMMON.CANCEL')}
                </Button>
              )}
              <Button type="submit">
                {data?.webhookId ? t('COMMON.SAVE') : t('COMMON.CREATE')}
              </Button>
            </div>
          )}
        </div>
      </form>
    </Form>
  );
};
