import { APP_SETTING_KEYS } from '@pinpoint-fe/ui/src/constants';
import { useLocalStorage } from './useLocalStorage';
import { isValidTimezone } from '@pinpoint-fe/ui/src/utils/date';
import { Dispatch, SetStateAction } from 'react';

export const useTimezone: () => [string, Dispatch<SetStateAction<string>>] = () => {
  const systemTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
  const timezoneStorage = useLocalStorage<string>(APP_SETTING_KEYS.TIMEZONE, systemTimezone);

  if (!isValidTimezone(timezoneStorage?.[0])) {
    window.localStorage.setItem(APP_SETTING_KEYS.TIMEZONE, JSON.stringify(systemTimezone));
    return [systemTimezone, timezoneStorage?.[1]];
  }

  return timezoneStorage;
};
