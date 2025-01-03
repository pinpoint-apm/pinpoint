import { APP_SETTING_KEYS, DATE_FORMATS } from '@pinpoint-fe/ui/constants';
import { format as dateFnsFormat } from 'date-fns';
import { getCompatibleLocalStorageValue } from './localStorage';
import { enUS, ko } from 'date-fns/locale';

const getLocale = () => {
  const language = getCompatibleLocalStorageValue(APP_SETTING_KEYS.LANGUAGE);

  if (language === 'ko') {
    return ko;
  } else {
    return enUS;
  }
};

export const getCurrentFormat = () => {
  const formatKeys = Object.keys(DATE_FORMATS).filter((v) => isNaN(Number(v)));
  const formatType: number = getCompatibleLocalStorageValue(APP_SETTING_KEYS.DATE_FORMAT) ?? 0;

  if (formatType > formatKeys.length || 0 > formatType) {
    return DATE_FORMATS[0];
  }
  return DATE_FORMATS[formatType];
};

export const format = (
  ...props: [date: Date | number, format?: string, option?: Parameters<typeof dateFnsFormat>['2']]
) => {
  const [date, formatStrProp, option] = props;
  const locale = getLocale();
  const formatStr = formatStrProp || getCurrentFormat();

  return dateFnsFormat(date, formatStr, {
    ...option,
    locale,
  });
};
