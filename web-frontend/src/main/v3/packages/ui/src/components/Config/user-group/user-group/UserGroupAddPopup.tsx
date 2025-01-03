import React from 'react';
import * as z from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import {
  Button,
  Input,
  Popover,
  PopoverContent,
  PopoverTrigger,
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '../../../ui';
import { useReactToastifyToast } from '../../../Toast';
import { usePostConfigUserGroup } from '@pinpoint-fe/ui/hooks';
import { TFunction } from 'i18next';
import { cn } from '../../../../lib';

const userGroupFormSchemaFactory = (t: TFunction) =>
  z.object({
    userGroupName: z
      .string({
        required_error: t('CONFIGURATION.USER_GROUP.VALIDATION'),
      })
      .min(4, { message: t('CONFIGURATION.USER_GROUP.VALIDATION') })
      .max(30, { message: t('CONFIGURATION.USER_GROUP.VALIDATION') })
      // eslint-disable-next-line
      .regex(/^[\w\-]{4,30}$/, { message: t('CONFIGURATION.USER_GROUP.VALIDATION') }),
  });

export interface UserGroupAddPopupProps {
  popupTrigger: React.ReactNode;
  onCompleteAdd?: (userGroupName: string) => void;
  userId: string;
}

export const UserGroupAddPopup = ({
  popupTrigger,
  onCompleteAdd,
  userId,
}: UserGroupAddPopupProps) => {
  const toast = useReactToastifyToast();
  const { t } = useTranslation();
  const [open, setOpen] = React.useState(false);
  const userGroupFormSchema = React.useMemo(() => userGroupFormSchemaFactory(t), [t]);
  const userGroupForm = useForm<z.infer<typeof userGroupFormSchema>>({
    resolver: zodResolver(userGroupFormSchema),
  });

  const { isMutating, onSubmit } = usePostConfigUserGroup({
    onCompleteSubmit: (userGroupName: string) => {
      toast.success(t('COMMON.SUBMIT_SUCCESS'), {
        autoClose: 2000,
      });

      setOpen(false);
      onCompleteAdd?.(userGroupName);
    },
    onError: () => {
      toast.error(t('COMMON.SUBMIT_FAIL'), {
        autoClose: 2000,
      });
    },
  });

  const handleSubmit = (userGroupFormData: z.infer<typeof userGroupFormSchema>) => {
    onSubmit({ id: userGroupFormData.userGroupName, userId });
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>{popupTrigger}</PopoverTrigger>
      <PopoverContent onEscapeKeyDown={(e) => e.preventDefault()}>
        <Form {...userGroupForm}>
          <form onSubmit={userGroupForm.handleSubmit(handleSubmit)} className="space-y-6">
            <FormField
              control={userGroupForm.control}
              name="userGroupName"
              render={({ field, fieldState }) => (
                <FormItem>
                  <FormLabel>{t('CONFIGURATION.USER_GROUP.USER_GROUP_ADD_TITLE')}</FormLabel>
                  <FormControl>
                    <Input
                      {...field}
                      className={cn('focus-visible:ring-0', {
                        'border-status-fail': fieldState.invalid,
                      })}
                      placeholder={t('CONFIGURATION.USER_GROUP.USER_GROUP_ADD_PLACEHOLDER')}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="flex justify-end gap-2">
              <Button variant="outline" type="button" onClick={() => setOpen(false)}>
                {t('COMMON.CANCEL')}
              </Button>
              <Button type="submit" disabled={isMutating}>
                {t('COMMON.SUBMIT')}
              </Button>
            </div>
          </form>
        </Form>
      </PopoverContent>
    </Popover>
  );
};
