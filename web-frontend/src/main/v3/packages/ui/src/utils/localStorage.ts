import { APP_SETTING_KEYS, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';
import { safeParse } from './json';

export const getLocalStorageValue = (key: APP_SETTING_KEYS | EXPERIMENTAL_CONFIG_KEYS | string) => {
  const v3Value = window.localStorage.getItem(key);
  const storageValue = safeParse(v3Value);

  return storageValue;
};
