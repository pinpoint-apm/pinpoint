import React from 'react';
import { useLocalStorage as useLocalStorageTS } from 'usehooks-ts';
import { APP_SETTING_KEYS, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/constants';
import { getCompatibleLocalStorageValue } from '@pinpoint-fe/ui/utils';

type LocalStorageKeyTypes = APP_SETTING_KEYS | EXPERIMENTAL_CONFIG_KEYS | string;

const getV2Key = (key: string) => {
  return `pp.${key}`;
};

export const useLocalStorage = <T, K extends LocalStorageKeyTypes = LocalStorageKeyTypes>(
  key: K,
  initialValue: T,
) => {
  const v2Key = getV2Key(key);
  const initValue = getCompatibleLocalStorageValue(key) ?? initialValue;

  const [data, setData] = useLocalStorageTS<T>(key, initValue);

  React.useEffect(() => {
    if (data !== undefined && data !== null) {
      window.localStorage.setItem(v2Key, JSON.stringify(data));
    }
  }, [data, v2Key]);

  return [data, setData] as [T, React.Dispatch<React.SetStateAction<T>>];
};

export const useExpiredLocalStorage = <
  T extends {
    value?: unknown;
    expire: number; // ms
  },
  K extends LocalStorageKeyTypes = LocalStorageKeyTypes,
>(
  key: K,
  initialValue?: T,
) => {
  const [data, setData] = useLocalStorage(key, initialValue);

  React.useEffect(() => {
    const now = new Date().getTime();

    if (data?.expire && data?.expire < now) {
      window.localStorage.removeItem(key);
      window.localStorage.removeItem(getV2Key(key));
    }
  }, [data]);

  return [data, setData] as [T, React.Dispatch<React.SetStateAction<T>>];
};
