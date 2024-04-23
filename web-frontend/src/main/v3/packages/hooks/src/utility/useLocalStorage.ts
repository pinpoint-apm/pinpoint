import React from 'react';
import { useLocalStorage as useLocalStorageTS } from 'usehooks-ts';
import { APP_SETTING_KEYS, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/constants';
import { getCompatibleLocalStorageValue } from '@pinpoint-fe/utils';

export const useLocalStorage = <T>(
  key: APP_SETTING_KEYS | EXPERIMENTAL_CONFIG_KEYS,
  initialValue: T,
) => {
  const v2Key = `pp.${key}`;
  const initValue = getCompatibleLocalStorageValue(key) ?? initialValue;

  const [data, setData] = useLocalStorageTS<T>(key, initValue);

  React.useEffect(() => {
    if (data !== undefined && data !== null) {
      window.localStorage.setItem(v2Key, JSON.stringify(data));
    }
  }, [data, v2Key]);

  return [data, setData] as [T, React.Dispatch<React.SetStateAction<T>>];
};
