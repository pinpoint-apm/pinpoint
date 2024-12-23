import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import { getCompatibleLocalStorageValue } from '@pinpoint-fe/ui/utils';
import { APP_SETTING_KEYS, en, ko } from '@pinpoint-fe/constants';

const resources = {
  en: {
    translation: en,
  },
  ko: {
    translation: ko,
  },
};

const systemLang = window.navigator.language.substring(0, 2);
const initLang = getCompatibleLocalStorageValue(APP_SETTING_KEYS.LANGUAGE);
const userLang = initLang ? initLang : systemLang.match(/en|ko/) ? systemLang : 'en';

i18n.use(initReactI18next).init({
  resources,
  lng: userLang, // default lang
  fallbackLng: 'en', // fallback lang
  interpolation: {
    escapeValue: false,
  },
});

export default i18n;
