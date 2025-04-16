import React from 'react';
import { useLocalStorage as useLocalStorageTS } from 'usehooks-ts';
import { APP_SETTING_KEYS, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';

type LocalStorageKeyTypes = APP_SETTING_KEYS | EXPERIMENTAL_CONFIG_KEYS | string;

export const useLocalStorage = <T, K extends LocalStorageKeyTypes = LocalStorageKeyTypes>(
  key: K,
  initialValue: T,
) => {
  const [data, setData] = useLocalStorageTS<T>(key, () => {
    try {
      const initValue = window.localStorage.getItem(key);

      if (initValue === null || initValue === undefined || initValue === 'undefined') {
        return initialValue;
      }

      return JSON.parse(initValue as any);
    } catch (error) {
      console.error('Error getting local storage item:', error);
      return initialValue;
    }
  });

  React.useEffect(() => {
    if (data !== undefined && data !== null && data !== 'undefined') {
      window.localStorage.setItem(key, JSON.stringify(data));
    }
  }, [data]);

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
    }
  }, [data]);

  return [data, setData] as [T, React.Dispatch<React.SetStateAction<T>>];
};
