import { enUS, ja, ko, zhCN, zhTW } from 'date-fns/locale';
import { LocaleKey } from '../types';
import { Locale } from 'date-fns';

type DistanceWords = Record<string, string>;

const EN_WORDS: DistanceWords = {
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

const KO_WORDS: DistanceWords = {
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

const makeFormatDistance =
  (
    words: DistanceWords,
    applySuffix: (result: string, isFuture: boolean) => string,
  ): Locale['formatDistance'] =>
  (token, count, options) => {
    const opts = options || {};
    const localed = words[token];
    // en 복수형은 count===1 일 때 끝의 s를 제거한다 (ko는 s가 없어 no-op).
    const grammared = count === 1 ? localed.replace(/s$/, '') : localed;
    const result = grammared.replace('{{count}}', String(count));

    if (opts.addSuffix) {
      return applySuffix(result, (opts.comparison ?? 0) > 0);
    }
    return result;
  };

export const getLocale = (localeKey: LocaleKey): Locale => {
  switch (localeKey) {
    case 'en':
      return {
        ...enUS,
        formatDistance: makeFormatDistance(EN_WORDS, (result, isFuture) =>
          isFuture ? `In ${result}` : `Past ${result}`,
        ),
      };
    case 'ko':
      return {
        ...ko,
        formatDistance: makeFormatDistance(KO_WORDS, (result, isFuture) =>
          isFuture ? `${result} 후` : `${result} 전`,
        ),
      };
    case 'ja':
      return {
        ...ja,
      };
    case 'zh':
    case 'zh-CN':
      return {
        ...zhCN,
      };
    case 'zh-TW':
      return {
        ...zhTW,
      };
    default:
      throw new Error(`Unsupported locale: ${localeKey}`);
  }
};

export const getLocaleKey = (locale: Locale): LocaleKey => {
  const code = locale.code || 'en';
  // 번체(zh-TW/zh-HK)와 간체(zh-CN)를 구분해서 되돌린다
  if (code.startsWith('zh')) {
    return code === 'zh-TW' || code === 'zh-HK' ? 'zh-TW' : 'zh-CN';
  }
  return (code.substring(0, 2) || 'en') as LocaleKey;
};

// UI 문구·퀵레인지 라벨에서 실제 지원하는 언어 집합
export type LabelLocale = 'en' | 'ko' | 'ja' | 'zh-CN' | 'zh-TW';

/** LocaleKey를 지원 언어로 정규화한다. ('zh'=간체 별칭, 미지원 언어는 en 폴백) */
export const resolveLocaleKey = (localeKey: LocaleKey): LabelLocale => {
  if (localeKey === 'zh') return 'zh-CN';
  if (localeKey === 'ko' || localeKey === 'ja' || localeKey === 'zh-CN' || localeKey === 'zh-TW') {
    return localeKey;
  }
  return 'en';
};
