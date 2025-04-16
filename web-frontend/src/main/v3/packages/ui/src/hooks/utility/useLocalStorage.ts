import React from 'react';
import { useLocalStorage as useLocalStorageTS } from 'usehooks-ts';
import { APP_SETTING_KEYS, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/ui/src/constants';

type LocalStorageKeyTypes = APP_SETTING_KEYS | EXPERIMENTAL_CONFIG_KEYS | string;

export const useLocalStorage = <T, K extends LocalStorageKeyTypes = LocalStorageKeyTypes>(
  key: K,
  initialValue: T,
) => {
  const [data, setData] = useLocalStorageTS<T>(key, initialValue);
  const [hydratedValue, setHydratedValue] = React.useState<T>(initialValue);

  React.useEffect(() => {
    const isInvalidValue =
      data === undefined || data === null || data === 'undefined' || data === '';

    if (isInvalidValue) {
      setData(initialValue);
      setHydratedValue(initialValue);
    } else {
      setHydratedValue(data as T);
    }
  }, [data, initialValue, setData]);

  return [hydratedValue, setData] as [T, React.Dispatch<React.SetStateAction<T>>];
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
