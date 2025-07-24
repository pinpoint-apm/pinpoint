import { APP_SETTING_KEYS, DATE_FORMATS } from '@pinpoint-fe/ui/src/constants';
import { formatInTimeZone } from 'date-fns-tz';
import { getLocalStorageValue } from './localStorage';
import { enUS, ko } from 'date-fns/locale';
import { isValidTimezone } from './date';

const getLocale = () => {
  const language = getLocalStorageValue(APP_SETTING_KEYS.LANGUAGE);

  if (language === 'ko') {
    return ko;
  } else {
    return enUS;
  }
};

export const getTimezone = () => {
  const timezone: string = getLocalStorageValue(APP_SETTING_KEYS.TIMEZONE);

  if (!isValidTimezone(timezone)) {
    const systemTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
    return systemTimezone;
  }

  return timezone;
};

export const getCurrentFormat = () => {
  const formatKeys = Object.keys(DATE_FORMATS).filter((v) => isNaN(Number(v)));
  const formatType: number = getLocalStorageValue(APP_SETTING_KEYS.DATE_FORMAT) ?? 0;

  if (formatType > formatKeys.length || 0 > formatType) {
    return DATE_FORMATS[0];
  }
  return DATE_FORMATS[formatType];
};

export const format = (
  ...props: [
    date: Date | number,
    format?: string,
    option?: Parameters<typeof formatInTimeZone>['3'],
  ]
) => {
  const [date, formatStrProp, option] = props;
  const locale = getLocale();
  const timezone = getTimezone();
  const formatStr = formatStrProp || getCurrentFormat();

  return formatInTimeZone(date, timezone, formatStr, {
    ...option,
    locale,
  });
};
