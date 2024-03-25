import { useTranslation } from 'react-i18next';
import { APP_SETTING_KEYS } from '@pinpoint-fe/constants';
import { useLocalStorage } from './useLocalStorage';
import React from 'react';

export type SupportLanguageType = 'en' | 'ko';

export const useLanguage = (initialValue: SupportLanguageType = 'en') => {
  const { i18n } = useTranslation();
  const languageStorage = useLocalStorage<SupportLanguageType>(
    APP_SETTING_KEYS.LANGUAGE,
    initialValue,
  );
  const [lang] = languageStorage;

  React.useEffect(() => {
    i18n.changeLanguage(lang);
  }, [lang, i18n]);

  return languageStorage;
};
