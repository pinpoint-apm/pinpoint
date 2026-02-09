import React from 'react';
import { useTranslation } from 'react-i18next';
import { RemovePopup } from '../../Popup';
import { AgentOverview, ApplicationType } from '@pinpoint-fe/ui/src/constants';
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
  Input,
} from '@pinpoint-fe/ui/src/components/ui';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const FormSchema = z.object({
  password: z.string(),
});

export interface AgentManagementRemovePopupProps {
  isApplication?: boolean;
  popupTrigger: React.ReactNode;
  application?: ApplicationType;
  agent?: AgentOverview.Instance;
  onClickRemove?: (
    removeTarget?: AgentOverview.Instance | ApplicationType,
    password?: string,
  ) => void;
}

export const AgentManagementRemovePopup = ({
  isApplication,
  popupTrigger,
  application,
  agent,
  onClickRemove,
}: AgentManagementRemovePopupProps) => {
  const { t } = useTranslation();

  const form = useForm<z.infer<typeof FormSchema>>({
    resolver: zodResolver(FormSchema),
    defaultValues: {
      password: '',
    },
  });

  function handleRemove() {
    const password = form.getValues('password');
    onClickRemove?.(isApplication ? application : agent, password);
  }

  return (
    <RemovePopup
      onOpenChange={(open) => {
        form.reset();
      }}
      popupTrigger={popupTrigger}
      popupTitle={t(
        `CONFIGURATION.AGENT_MANAGEMENT.${
          isApplication ? 'REMOVE_APPLICATION_TITLE' : 'REMOVE_AGENT_TITLE'
        }`,
      )}
      popupDesc={t(
        `CONFIGURATION.AGENT_MANAGEMENT.${
          isApplication ? 'REMOVE_APPLICATION_DESC' : 'REMOVE_AGENT_DESC'
        }`,
      )}
      popupContents={
        <div className="flex flex-col gap-2 text-sm font-semibold">
          <div>
            {isApplication ? application?.applicationName : agent?.hostName}{' '}
            {!isApplication && <span className="text-muted-foreground">({agent?.agentId})</span>}
          </div>
          <div>
            <Form {...form}>
              <form className="space-y-6 w-full">
                <FormField
                  control={form.control}
                  name="password"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel className="text-xs text-muted-foreground">Password</FormLabel>
                      <FormControl>
                        <Input type="password" {...field} />
                      </FormControl>
                      <FormDescription>
                        {t('CONFIGURATION.AGENT_MANAGEMENT.REMOVE_PASSWORD_DESCRIPTION')}
                      </FormDescription>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </form>
            </Form>
          </div>
        </div>
      }
      onClickRemove={() => handleRemove()}
    />
  );
};
