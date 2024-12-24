import { APP_SETTING_KEYS, DATE_FORMATS } from '@pinpoint-fe/ui/constants';
import { useLocalStorage } from './useLocalStorage';
import React from 'react';

export const useDateFormat = (
  initialValue = 0,
): [string, React.Dispatch<React.SetStateAction<DATE_FORMATS>>] => {
  const dateFormatStorage = useLocalStorage<DATE_FORMATS>(
    APP_SETTING_KEYS.DATE_FORMAT,
    initialValue,
  );

  return [DATE_FORMATS[dateFormatStorage[0]], dateFormatStorage[1]];
};
