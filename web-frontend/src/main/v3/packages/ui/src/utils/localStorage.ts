import { APP_SETTING_KEYS, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';

export const getCompatibleLocalStorageValue = (
  key: APP_SETTING_KEYS | EXPERIMENTAL_CONFIG_KEYS | string,
) => {
  const v2Key = `pp.${key}`;
  const v2Value = window.localStorage.getItem(v2Key);
  const v3Value = window.localStorage.getItem(key);
  const storageValue = (v2Value && JSON.parse(v2Value)) ?? (v3Value && JSON.parse(v3Value));

  return storageValue;
};
