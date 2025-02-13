import { enUS, ja, ko, zhCN } from 'date-fns/locale';
import { LocaleKey } from '../types';
import { Locale } from 'date-fns';

export const getLocale = (localeKey: LocaleKey): Locale => {
  switch (localeKey) {
    case 'en':
      return {
        ...enUS,
        formatDistance: (token, count, options) => {
          options = options || {};

          const formatDistanceLocale = {
            lessThanXSeconds: '{{count}} Seconds',
            xSeconds: '{{count}} Seconds',
            halfAMinute: '30 Seconds',
            lessThanXMinutes: '{{count}} Minutes',
            xMinutes: '{{count}} Minutes',
            aboutXHours: '{{count}} Hours',
            xHours: '{{count}} Hours',
            xDays: '{{count}} Days',
            aboutXWeeks: '{{count}} Weeks',
            xWeeks: '{{count}} Weeks',
            aboutXMonths: '{{count}} Months',
            xMonths: '{{count}} Months',
            aboutXYears: '{{count}} Years',
            xYears: '{{count}} Years',
            overXYears: '{{count}} Years',
            almostXYears: '{{count}} Years',
          };

          const localed = formatDistanceLocale[token as keyof typeof formatDistanceLocale];
          const grammared = count === 1 ? localed.replace(/s$/, '') : localed;
          const result = grammared.replace('{{count}}', count);

          if (options.addSuffix) {
            if (options.comparison > 0) {
              return 'In ' + result;
            } else {
              return 'Past ' + result;
            }
          }

          return result;
        },
      };
    case 'ko':
      return {
        ...ko,
        formatDistance: (token, count, options) => {
          options = options || {};

          const formatDistanceLocale = {
            lessThanXSeconds: '{{count}}초',
            xSeconds: '{{count}}초',
            halfAMinute: '30초',
            lessThanXMinutes: '{{count}}분',
            xMinutes: '{{count}}분',
            aboutXHours: '{{count}}시간',
            xHours: '{{count}}시간',
            xDays: '{{count}}일',
            aboutXWeeks: '{{count}}주',
            xWeeks: '{{count}}주',
            aboutXMonths: '{{count}}개월',
            xMonths: '{{count}}개월',
            aboutXYears: '{{count}}년',
            xYears: '{{count}}년',
            overXYears: '{{count}}년',
            almostXYears: '{{count}}년',
          };

          const localed = formatDistanceLocale[token as keyof typeof formatDistanceLocale];
          const grammared = count === 1 ? localed.replace(/s$/, '') : localed;
          const result = grammared.replace('{{count}}', count);

          if (options.addSuffix) {
            if (options.comparison > 0) {
              return result + ' 후';
            } else {
              return result + ' 전';
            }
          }

          return result;
        },
      };
    case 'ja':
      return {
        ...ja,
      };
    case 'zh':
      return {
        ...zhCN,
      };
    default:
      throw new Error(`Unsupported locale: ${localeKey}`);
  }
};

export const getLocaleKey = (locale: Locale) => {
  return (locale.code?.substring(0, 2) || 'en') as LocaleKey;
};
