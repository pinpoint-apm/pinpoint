import { DateRange, LocaleKey, TimePatternKeys, TimeUnit, TimeUnitFormat } from '../types';
import {
  endOfDay,
  endOfMonth,
  endOfYear,
  isDate,
  isValid,
  parse,
  startOfDay,
  startOfMonth,
  startOfYear,
  subDays,
  subHours,
  subMinutes,
  subMonths,
  subSeconds,
  subWeeks,
  subYears,
} from 'date-fns';
import type { Locale } from 'date-fns';
import { removeSpaces } from './string';
import { LabelLocale, resolveLocaleKey } from './locale';
import { SEAM_TOKEN, dateFormats, timePatterns } from '../constants/patterns';
import { toZonedTime, fromZonedTime, formatInTimeZone } from 'date-fns-tz';

// 단위 → 밀리초. convertToMilliseconds / convertToTimeUnit 양방향의 단일 소스.
// mo=30일, y=365일로 근사한다 (두 함수가 동일 정의를 공유해 라벨↔ms 왕복이 안정적).
const DAY_MS = 24 * 60 * 60 * 1000;
const MS_PER_UNIT: Record<TimeUnit, number> = {
  s: 1000,
  m: 60 * 1000,
  h: 60 * 60 * 1000,
  d: DAY_MS,
  w: 7 * DAY_MS,
  mo: 30 * DAY_MS,
  y: 365 * DAY_MS,
};

export const convertToMilliseconds = (timeUnit: TimeUnitFormat | number, timeZone: string) => {
  if (typeof timeUnit === 'number') {
    return timeUnit;
  } else if (timeUnit === 'today') {
    const [from, to] = getDateFromPatternedString(timeUnit, timeZone) as [Date, Date];

    return to.getTime() - from.getTime();
  } else {
    const arr = timeUnit.match(/(\d+)([a-z]+)/i);

    if (arr) {
      const timeNumber = parseInt(arr[1], 10);
      const ms = MS_PER_UNIT[arr[2] as TimeUnit];
      if (ms !== undefined) {
        return timeNumber * ms;
      }
    }
    throw new Error('Invalid time unit provided.');
  }
};

export const convertToTimeUnit = (milliseconds = 0): TimeUnitFormat => {
  const seconds = Math.ceil(milliseconds / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  const weeks = Math.floor(days / 7);
  const months = Math.floor(days / 30);
  const years = Math.floor(days / 365);

  if (years >= 1) return `${years}y`;
  if (months >= 1) return `${months}mo`;
  if (weeks >= 1) return `${weeks}w`;
  if (days >= 1) return `${days}d`;
  if (hours >= 1) return `${hours}h`;
  if (minutes >= 1) return `${minutes}m`;
  return `${seconds}s`;
};

export const getFormattedTimeUnit = (milliseconds: number, formatter?: (ms: number) => string) =>
  formatter ? formatter(milliseconds) : convertToTimeUnit(milliseconds);

// 각 단위의 [단수, 복수]. en만 복수형이 다르고 ko/ja/zh는 동일.
const UNIT_WORDS: Record<LabelLocale, Record<TimeUnit, [string, string]>> = {
  en: {
    s: ['second', 'seconds'],
    m: ['minute', 'minutes'],
    h: ['hour', 'hours'],
    d: ['day', 'days'],
    w: ['week', 'weeks'],
    mo: ['month', 'months'],
    y: ['year', 'years'],
  },
  ko: {
    s: ['초', '초'],
    m: ['분', '분'],
    h: ['시간', '시간'],
    d: ['일', '일'],
    w: ['주', '주'],
    mo: ['개월', '개월'],
    y: ['년', '년'],
  },
  ja: {
    s: ['秒', '秒'],
    m: ['分', '分'],
    h: ['時間', '時間'],
    d: ['日', '日'],
    w: ['週間', '週間'],
    mo: ['か月', 'か月'],
    y: ['年', '年'],
  },
  'zh-CN': {
    s: ['秒', '秒'],
    m: ['分钟', '分钟'],
    h: ['小时', '小时'],
    d: ['天', '天'],
    w: ['周', '周'],
    mo: ['个月', '个月'],
    y: ['年', '年'],
  },
  'zh-TW': {
    s: ['秒', '秒'],
    m: ['分鐘', '分鐘'],
    h: ['小時', '小時'],
    d: ['天', '天'],
    w: ['週', '週'],
    mo: ['個月', '個月'],
    y: ['年', '年'],
  },
};

const TODAY_WORD: Record<LabelLocale, string> = {
  en: 'Today',
  ko: '오늘',
  ja: '今日',
  'zh-CN': '今天',
  'zh-TW': '今天',
};

// "Last N unit" 스타일 문장 조립 (언어별 어순/공백 차이)
const LABEL_FORMAT: Record<LabelLocale, (count: number, word: string) => string> = {
  en: (count, word) => `Last ${count} ${word}`,
  ko: (count, word) => `최근 ${count}${word}`,
  ja: (count, word) => `過去${count}${word}`,
  'zh-CN': (count, word) => `最近${count}${word}`,
  'zh-TW': (count, word) => `最近${count}${word}`,
};

/**
 * 퀵레인지 토큰(예: '60m', '7d')을 "Last 60 minutes" / "최근 7일" / "過去60分" 같은 라벨로 변환한다.
 * formatDistance와 달리 토큰의 수치를 근사 없이 그대로 보존한다. (en/ko/ja/zh-CN/zh-TW 지원)
 */
export const getQuickRangeLabel = (timeUnit: TimeUnitFormat, localeKey: LocaleKey): string => {
  const key = resolveLocaleKey(localeKey);
  if (timeUnit === 'today') return TODAY_WORD[key];

  const matched = timeUnit.match(/(\d+)([a-z]+)/i);
  if (!matched) return timeUnit;

  const count = parseInt(matched[1], 10);
  const words = UNIT_WORDS[key][matched[2] as TimeUnit];
  if (!words) return timeUnit;

  const word = key === 'en' && count !== 1 ? words[1] : words[0];
  return LABEL_FORMAT[key](count, word);
};

export const parseDateString = ({
  dateString,
  locale,
  timeZone,
  dateFormat,
  referenceDate = new Date(),
}: {
  dateString: string;
  locale: Locale;
  timeZone: string;
  dateFormat?: string;
  referenceDate?: Date;
}) => {
  const formats = dateFormat ? [dateFormat, ...dateFormats] : dateFormats;

  const targetFormat =
    formats.find((f) =>
      isValid(
        parseWithTimeZone(removeSpaces(dateString), removeSpaces(f), referenceDate, timeZone, {
          locale,
        }),
      ),
    ) || '';
  const parsedDate = parseWithTimeZone(
    removeSpaces(dateString),
    removeSpaces(targetFormat),
    referenceDate,
    timeZone,
    {
      locale,
    },
  );

  return parsedDate;
};

export const parseTimeString = (
  dateString: string,
  locale: Locale,
  {
    timeZone,
    dateFormat,
    referenceDate,
    seamToken = SEAM_TOKEN,
  }: { dateFormat?: string; seamToken?: string; referenceDate?: Date | Date[]; timeZone: string },
) => {
  // patterned string?
  let dateRange: DateRange | undefined = getDateFromPatternedString(dateString, timeZone);
  if (!dateRange) {
    if (dateString.includes(seamToken)) {
      // date Range
      const dates = dateString.split(seamToken);
      const from = dates[0];
      const to = dates[1];
      let parsedFrom = parseDateString({
        dateString: from,
        locale,
        timeZone,
        dateFormat,
        referenceDate: Array.isArray(referenceDate)
          ? isDate(referenceDate?.[0])
            ? referenceDate[0]
            : undefined
          : undefined,
      });
      let parsedTo = parseDateString({
        dateString: to,
        locale,
        timeZone,
        dateFormat,
        referenceDate: Array.isArray(referenceDate)
          ? isDate(referenceDate?.[1])
            ? referenceDate[1]
            : undefined
          : undefined,
      });

      if (isDayFormat(from.trim())) {
        parsedFrom = getZonedStartOfDay(parsedFrom, timeZone);
      }

      if (isDayFormat(to.trim())) {
        parsedTo = getZonedEndOfDay(parsedTo, timeZone);
      }

      dateRange = [parsedFrom, parsedTo];
    } else {
      // singe date

      const parsedDate = parseDateString({
        dateString,
        locale,
        timeZone,
        dateFormat,
        referenceDate: isDate(referenceDate) ? (referenceDate as Date) : undefined,
      });
      dateRange = [
        getZonedStartOfDay(parsedDate, timeZone),
        getZonedEndOfDay(parsedDate, timeZone),
      ];
    }
  }
  return dateRange;
};

export const getMatchedPatternKey = (dateString: string) => {
  const matchedKey = (Object.keys(timePatterns) as TimePatternKeys[]).find((key) => {
    const patterns = timePatterns[key];
    return patterns.some((regex) => regex.test(dateString));
  });

  return matchedKey;
};

export const getDateFromPatternedString = (
  dateString: string,
  timeZone: string,
  referenceDate = new Date(),
): DateRange | undefined => {
  const matchedKey = getMatchedPatternKey(dateString);

  switch (matchedKey) {
    case 'second': {
      const seconds = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subSeconds(referenceDate, seconds), referenceDate];
    }
    case 'minute': {
      const minutes = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subMinutes(referenceDate, minutes), referenceDate];
    }
    case 'hour': {
      const hours = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subHours(referenceDate, hours), referenceDate];
    }
    case 'day': {
      const days = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subDays(referenceDate, days), referenceDate];
    }
    case 'week': {
      const weeks = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subWeeks(referenceDate, weeks), referenceDate];
    }
    case 'year': {
      const year = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subYears(referenceDate, year), referenceDate];
    }
    case 'month': {
      const month = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subMonths(referenceDate, month), referenceDate];
    }
    case 'yesterday': {
      const yesterday = subDays(referenceDate, 1);
      return [getZonedStartOfDay(yesterday, timeZone), getZonedEndOfDay(yesterday, timeZone)];
    }
    case 'today': {
      return [getZonedStartOfDay(referenceDate, timeZone), referenceDate];
    }
    case 'lastMonth': {
      const lastMonth = subMonths(referenceDate, 1);
      return [getZonedStartOfMonth(lastMonth, timeZone), getZonedEndOfMonth(lastMonth, timeZone)];
    }
    case 'lastYear': {
      const lastYear = subYears(referenceDate, 1);
      return [getZonedStartOfYear(lastYear, timeZone), getZonedEndOfYear(lastYear, timeZone)];
    }
    case 'unixTimestampRange': {
      const pattern = /(\d{13})\s*[^\d]\s*(\d{13})/;
      const match = dateString.match(pattern);
      if (!match) return;

      const startDate = new Date(parseInt(match[1]));
      const endDate = new Date(parseInt(match[2]));
      return [startDate, endDate];
    }
    default:
      return;
  }
};

export const isDayFormat = (dateString: string) => {
  const dayPatterns = [/^[A-Za-z]{3} \d{1,2}$/, /^\d{1,2}\/\d{1,2}$/, /^\d{1,2}\. \d{1,2}$/];

  return dayPatterns.some((pattern) => pattern.test(dateString));
};

export const calcZonedDate = (date: Date, tz: string, fn: (date: Date) => Date) => {
  const inputZoned = toZonedTime(date, tz);
  const fnZoned = fn(inputZoned);
  return fromZonedTime(fnZoned, tz);
};

export const getZonedStartOfDay = (date: Date, timeZone: string) => {
  return calcZonedDate(date, timeZone, startOfDay);
};

export const getZonedEndOfDay = (date: Date, timeZone: string) => {
  return calcZonedDate(date, timeZone, endOfDay);
};

export const getZonedStartOfMonth = (date: Date, timeZone: string) => {
  return calcZonedDate(date, timeZone, startOfMonth);
};

export const getZonedEndOfMonth = (date: Date, timeZone: string) => {
  return calcZonedDate(date, timeZone, endOfMonth);
};

export const getZonedStartOfYear = (date: Date, timeZone: string) => {
  return calcZonedDate(date, timeZone, startOfYear);
};

export const getZonedEndOfYear = (date: Date, timeZone: string) => {
  return calcZonedDate(date, timeZone, endOfYear);
};

/**
 * 날짜(연/월/일)는 timeZone 기준으로 보존하고 시각(시/분/초)만 교체한다.
 * `setHours` 계열은 시스템 로컬 기준이라 non-local timeZone에서 벽시계가 깨지므로,
 * timeZone 기준 문자열로 재조립한 뒤 fromZonedTime으로 UTC Date를 만든다.
 */
export const setZonedTimeOfDay = (
  date: Date,
  timeZone: string,
  time: { h: number; m: number; s: number },
) => {
  const pad = (n: number) => String(Math.trunc(n)).padStart(2, '0');
  const datePart = formatInTimeZone(date, timeZone, 'yyyy-MM-dd');
  return fromZonedTime(`${datePart} ${pad(time.h)}:${pad(time.m)}:${pad(time.s)}`, timeZone);
};

export const parseWithTimeZone = (
  dateString: string,
  format: string,
  referenceDate: Date,
  timeZone: string,
  options: Parameters<typeof parse>['3'],
) => {
  const zonedDate = toZonedTime(referenceDate, timeZone);
  const parsedDate = parse(dateString, format, zonedDate, options);
  return fromZonedTime(parsedDate, timeZone);
};
