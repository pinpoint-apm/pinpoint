import { APP_SETTING_KEYS, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';

const parseSafe = (value: string | null) => {
  if (value === null || value === undefined || value === 'undefined') {
    return undefined;
  }
  try {
    return JSON.parse(value);
  } catch {
    return undefined;
  }
};

export const getLocalStorageValue = (key: APP_SETTING_KEYS | EXPERIMENTAL_CONFIG_KEYS | string) => {
  const v3Value = window.localStorage.getItem(key);
  const storageValue = parseSafe(v3Value);

  return storageValue;
};
