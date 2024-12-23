import React from 'react';
import * as z from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { TFunction } from 'i18next';

import { ConfigUsers } from '@pinpoint-fe/constants';
import { PhoneInput } from 'react-international-phone';

import 'react-international-phone/style.css';
import { PhoneNumberUtil } from 'google-libphonenumber';
import { extractStringAfterSubstring } from '@pinpoint-fe/ui/utils';
import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
  Optional,
  Button,
  Input,
  Form,
} from '../../../components';
import { cn } from '../../../lib';

type userFormSchemaKey = 'userId' | 'userName' | 'department' | 'phoneNumber' | 'email';
const userFormSchemaFactory = (
  t: TFunction,
  validation: Partial<Record<userFormSchemaKey, (value: string) => boolean>>,
) =>
  z.object({
    userId: z
      .string({
        required_error: t('CONFIGURATION.USERS.VALIDATION.USER_ID'),
      })
      .min(4, { message: t('CONFIGURATION.USERS.VALIDATION.USER_ID') })
      .max(24, { message: t('CONFIGURATION.USERS.VALIDATION.USER_ID') })
      // eslint-disable-next-line
      .regex(/^[a-z0-9\_\-]{4,24}$/, { message: t('CONFIGURATION.USERS.VALIDATION.USER_ID') }),
    userName: z
      .string({
        required_error: t('CONFIGURATION.USERS.VALIDATION.USER_NAME'),
      })
      .min(1, { message: t('CONFIGURATION.USERS.VALIDATION.USER_NAME') })
      .max(30, { message: t('CONFIGURATION.USERS.VALIDATION.USER_NAME') })
      // eslint-disable-next-line
      .regex(/^[\w\-\.ㄱ-ㅎ|ㅏ-ㅣ|가-힣]{1,30}$/, {
        message: t('CONFIGURATION.USERS.VALIDATION.USER_NAME'),
      }),
    department: z
      .string()
      .min(3, { message: t('CONFIGURATION.USERS.VALIDATION.USER_DEPARTMENT') })
      .max(40, { message: t('CONFIGURATION.USERS.VALIDATION.USER_DEPARTMENT') })
      // eslint-disable-next-line
      .regex(/^[\w\.\-ㄱ-ㅎ|ㅏ-ㅣ|가-힣]{3,40}$/, {
        message: t('CONFIGURATION.USERS.VALIDATION.USER_DEPARTMENT'),
      })
      .optional()
      .or(z.literal('')),
    phoneNumber: z
      .string()
      .refine(validation.phoneNumber!, {
        message: t('CONFIGURATION.USERS.VALIDATION.USER_PHONE_NUMBER'),
      })
      .optional()
      .or(z.literal('')),
    email: z
      .string()
      .email({ message: t('CONFIGURATION.USERS.VALIDATION.USER_EMAIL') })
      .optional()
      .or(z.literal('')),
  });

export interface UserFormProps {
  userInfo?: ConfigUsers.User;
  enableUserEdit?: boolean;
  hideCancelButton?: boolean;
  onSubmit?: (arg: ConfigUsers.User) => void;
  onClickCancel?: () => void;
}

export const UserForm = ({
  userInfo,
  enableUserEdit = false,
  hideCancelButton = false,
  onClickCancel,
  onSubmit,
}: UserFormProps) => {
  const defaultValues = React.useMemo(() => {
    return {
      userId: userInfo?.userId || '',
      userName: userInfo?.name || '',
      department: userInfo?.department || '',
      phoneNumber: userInfo ? `${userInfo?.phoneCountryCode}${userInfo?.phoneNumber}` : '',
      email: userInfo?.email || '',
    };
  }, [userInfo]);
  const { t } = useTranslation();
  const userCountryDialCode = React.useRef<string>();
  const phoneUtil = React.useMemo(() => PhoneNumberUtil.getInstance(), []);
  const isPhoneValid = React.useCallback((phone: string) => {
    if (extractStringAfterSubstring(phone, userCountryDialCode?.current) === '') {
      return true;
    }
    try {
      return phoneUtil.isValidNumber(phoneUtil.parseAndKeepRawInput(phone));
    } catch (e) {
      return false;
    }
  }, []);
  const userFormSchema = React.useMemo(
    () => userFormSchemaFactory(t, { phoneNumber: isPhoneValid }),
    [t],
  );
  const userForm = useForm<z.infer<typeof userFormSchema>>({
    resolver: zodResolver(userFormSchema),
    defaultValues,
  });
  React.useEffect(() => {
    userForm.reset(defaultValues);
  }, [defaultValues, userForm]);

  const handleSubmit = (userData: z.infer<typeof userFormSchema>) => {
    const userPhoneNumber = extractStringAfterSubstring(
      userData.phoneNumber,
      `+${userCountryDialCode.current}`,
    );

    onSubmit?.({
      ...userData,
      name: userData.userName,
      phoneCountryCode: userPhoneNumber === '' ? '' : userCountryDialCode.current,
      phoneNumber: userPhoneNumber,
    });
  };

  const handleCancel = () => {
    onClickCancel?.();
  };

  return (
    <div className={cn('px-5 py-3')}>
      <Form {...userForm}>
        <form onSubmit={userForm.handleSubmit(handleSubmit)} className="space-y-6">
          <FormField
            control={userForm.control}
            name="userId"
            render={({ field, fieldState }) => (
              <FormItem>
                <FormLabel>{t('CONFIGURATION.USERS.LABEL.USER_ID')}</FormLabel>
                <FormControl>
                  <Input
                    {...field}
                    className={cn('focus-visible:ring-0', {
                      'border-status-fail': fieldState.invalid,
                    })}
                    readOnly={!!userInfo}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={userForm.control}
            name="userName"
            render={({ field, fieldState }) => (
              <FormItem>
                <FormLabel>{t('CONFIGURATION.USERS.LABEL.USER_NAME')}</FormLabel>
                <FormControl>
                  <Input
                    {...field}
                    className={cn('focus-visible:ring-0', {
                      'border-status-fail': fieldState.invalid,
                    })}
                    readOnly={!enableUserEdit}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={userForm.control}
            name="department"
            render={({ field, fieldState }) => (
              <FormItem>
                <FormLabel>
                  <Optional hide={!!userInfo}>
                    {t('CONFIGURATION.USERS.LABEL.USER_DEPARTMENT')}
                  </Optional>
                </FormLabel>
                <FormControl>
                  <Input
                    {...field}
                    className={cn('focus-visible:ring-0', {
                      'border-status-fail': fieldState.invalid,
                    })}
                    readOnly={!enableUserEdit}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={userForm.control}
            name="phoneNumber"
            render={({ field, fieldState }) => (
              <FormItem>
                <FormLabel>
                  <Optional hide={!!userInfo}>
                    {t('CONFIGURATION.USERS.LABEL.USER_PHONE_NUMBER')}
                  </Optional>
                </FormLabel>
                <FormControl>
                  <PhoneInput
                    className={cn(
                      'text-sm bg-transparent border rounded-md shadow-sm border-input focus-visible:ring-0',
                      { 'border-status-fail': fieldState.invalid },
                    )}
                    defaultCountry="kr"
                    countrySelectorStyleProps={{
                      className: 'border-0',
                      buttonClassName: 'border-0',
                    }}
                    inputClassName="flex-1 !py-1 !border-0"
                    forceDialCode={true}
                    {...field}
                    onChange={(phone, meta) => {
                      userCountryDialCode.current = meta.country.dialCode;
                      field.onChange(phone, meta);
                    }}
                    inputProps={{
                      readOnly: !enableUserEdit,
                    }}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={userForm.control}
            name="email"
            render={({ field, fieldState }) => (
              <FormItem>
                <FormLabel>
                  <Optional hide={!!userInfo}>{t('CONFIGURATION.USERS.LABEL.USER_EMAIL')}</Optional>
                </FormLabel>
                <FormControl>
                  <Input
                    {...field}
                    className={cn('focus-visible:ring-0', {
                      'border-status-fail': fieldState.invalid,
                    })}
                    readOnly={!enableUserEdit}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <div className="flex justify-end gap-2">
            {!hideCancelButton && (
              <Button variant="outline" type="button" onClick={handleCancel}>
                {t('COMMON.CANCEL')}
              </Button>
            )}
            <Button
              variant={enableUserEdit ? 'default' : 'secondary'}
              type="submit"
              disabled={!enableUserEdit}
            >
              {t('COMMON.SUBMIT')}
            </Button>
          </div>
        </form>
      </Form>
    </div>
  );
};
