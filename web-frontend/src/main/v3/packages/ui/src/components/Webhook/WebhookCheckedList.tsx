import React from 'react';
import { ErrorBoundary } from '../Error/ErrorBoundary';
import { WebhookListFetcher, WebhookListFetcherProps } from './WebhookListFetcher';
import { Checkbox } from '../ui/checkbox';
import { useGetWebhook } from '@pinpoint-fe/ui/hooks';
import { ScrollArea } from '../../components/ui/scroll-area';
import { cn } from '../../lib/utils';
import { Label } from '../../components';
import { Link } from 'react-router-dom';
import { APP_PATH } from '@pinpoint-fe/constants';

export interface WebhookCheckedListProps extends WebhookListFetcherProps {
  disabled?: boolean;
  onCheckedChange?: (props: string[]) => void;
}

export const WebhookCheckedList = ({
  onCheckedChange,
  disabled,
  disableFetch,
  ...props
}: WebhookCheckedListProps) => {
  const { data: usedWebhookList } = useGetWebhook({
    ruleId: props?.ruleId,
    disableFetch: !!disableFetch,
  });
  const [checkedWebhook, setCheckedWebhook] = React.useState<string[]>([]);

  React.useEffect(() => {
    const used = usedWebhookList?.map((webhook) => webhook.webhookId) as string[];

    setCheckedWebhook(used || []);
  }, [usedWebhookList]);

  React.useEffect(() => {
    onCheckedChange?.(checkedWebhook);
  }, [checkedWebhook]);

  return (
    <ErrorBoundary>
      <React.Suspense fallback={'loading'}>
        <WebhookListFetcher {...props}>
          {(webhookList) =>
            webhookList && webhookList.length > 0 ? (
              <ScrollArea
                className={cn('border rounded h-52', {
                  'border-input opacity-70': disabled,
                })}
              >
                <div className="p-3 space-y-2">
                  {webhookList.map((webhook, i) => (
                    <div key={i} className="flex items-center space-x-2">
                      <Checkbox
                        disabled={disabled}
                        id={webhook.webhookId}
                        checked={checkedWebhook.includes(webhook.webhookId as string)}
                        onCheckedChange={(check) => {
                          if (check) {
                            setCheckedWebhook((prev) => {
                              const next = [...prev, webhook.webhookId as string];
                              return next;
                            });
                          } else {
                            setCheckedWebhook((prev) => {
                              const next = prev.filter((id) => id !== webhook.webhookId);
                              return next;
                            });
                          }
                        }}
                      />
                      <Label htmlFor={webhook.webhookId}>
                        {webhook.alias} ({webhook.url})
                      </Label>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            ) : (
              <div
                className={cn('p-4 border rounded min-h-[3.75rem] text-sm', {
                  'border-input opacity-50': disabled,
                })}
              >
                No Registered Webhook. If you want to register webhook, open webhook settings or go
                to{' '}
                <Link
                  className={cn('hover:underline text-primary', {
                    ' pointer-events-none': disabled,
                  })}
                  to={APP_PATH.CONFIG_WEBHOOK}
                >
                  webhook configuration page
                </Link>
                .
              </div>
            )
          }
        </WebhookListFetcher>
      </React.Suspense>
    </ErrorBoundary>
  );
};
