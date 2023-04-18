import React from 'react';

import { APP_SETTING_KEYS, EXPERIMENTAL_CONFIG_KEYS } from '@pinpoint-fe/constants';

export const useLocalStorage = <T>(key: APP_SETTING_KEYS | EXPERIMENTAL_CONFIG_KEYS, initialValue: T) => {
  const [storedValue, setStoredValue] = React.useState<T>(initialValue);

  React.useEffect(() => {
    const item = window.localStorage.getItem(key);

    item && setStoredValue(JSON.parse(item))
  }, []);

  const setValue = (value: T) => {
    try {
      // Allow value to be a function so we have same API as useState
      const valueToStore = value instanceof Function ? value(storedValue) : value;
      // Save state
      setStoredValue(valueToStore);
      // Save to local storage
      window?.localStorage?.setItem(key, JSON.stringify(valueToStore));
    } catch (error) {
      // A more advanced implementation would handle the error case
      console.log(error);
    }
  };

  return [storedValue, setValue] as const;
}
