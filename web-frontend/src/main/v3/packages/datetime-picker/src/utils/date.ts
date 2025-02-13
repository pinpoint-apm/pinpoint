import { DateRange, TimePatternKeys, TimeUnitFormat } from '../types';
import {
  endOfDay,
  endOfMonth,
  endOfYear,
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
import { removeSpaces } from './string';
import { SEAM_TOKEN, dateFormats, timePatterns } from '../constants/patterns';
import { utcToZonedTime, zonedTimeToUtc } from 'date-fns-tz';

// const TimeUnitToMillisecondsMap: { [key in TimeUnit]: number } = {
//   s: 1000,
//   m: 60 * 1000,
//   h: 60 * 60 * 1000,
//   d: 24 * 60 * 60 * 1000,
//   w: 7 * 24 * 60 * 60 * 1000,
//   mo: 30 * 60 * 60 * 1000,
// };

export const convertToMilliseconds = (timeUnit: TimeUnitFormat | number, timeZone: string) => {
  if (typeof timeUnit === 'number') {
    return timeUnit;
  } else if (timeUnit === 'today') {
    const [from, to] = getDateFromPatternedString(timeUnit, timeZone) as [Date, Date];

    return to.getTime() - from.getTime();
  } else {
    const arr = timeUnit.match(/(\d+)([a-z]+)/i);

    if (Array.isArray(arr)) {
      const timeNumber = parseInt(arr[1], 10);
      const unit = arr[2];

      switch (unit) {
        case 's':
          return timeNumber * 1000;
        case 'm':
          return timeNumber * 60 * 1000;
        case 'h':
          return timeNumber * 60 * 60 * 1000;
        case 'd':
          return timeNumber * 24 * 60 * 60 * 1000;
        case 'w':
          return timeNumber * 7 * 24 * 60 * 60 * 1000;
        case 'mo':
          return timeNumber * 30 * 24 * 60 * 60 * 1000;
        case 'y':
          return timeNumber * 365 * 24 * 60 * 60 * 1000;
        default:
          break;
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
  const years = Math.floor(months / 12);

  if (years >= 1) {
    return `${years}y`;
  } else if (months >= 1) {
    return `${months}mo`;
  } else if (weeks >= 1) {
    return `${weeks}w`;
  } else if (days >= 2) {
    return `${days}d`;
  } else if (days === 1) {
    return `1d`;
  } else if (hours >= 1) {
    const remainingHours = hours % 24;
    return remainingHours > 0 ? `${remainingHours}h` : `${hours}h`;
  } else if (minutes >= 1) {
    const remainingMinutes = minutes % 60;
    return remainingMinutes > 0 ? `${remainingMinutes}m` : `${minutes}m`;
  } else {
    return `${seconds}s`;
  }
};

export const getFormattedTimeUnit = (milliseconds: number, formatter?: (ms: number) => string) => {
  const formattedTimeUnit = formatter ? formatter?.(milliseconds) : convertToTimeUnit(milliseconds);
  return formattedTimeUnit;
};

export const parseDateString = (
  dateString: string,
  locale: Locale,
  timeZone: string,
  dateFormat?: string,
) => {
  const formats = dateFormat ? [dateFormat, ...dateFormats] : dateFormats;

  const targetFormat =
    formats.find((f) =>
      isValid(
        parseWithTimeZone(removeSpaces(dateString), removeSpaces(f), new Date(), timeZone, {
          locale,
        }),
      ),
    ) || '';
  const parsedDate = parseWithTimeZone(
    removeSpaces(dateString),
    removeSpaces(targetFormat),
    new Date(),
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
    seamToken = SEAM_TOKEN,
  }: { dateFormat?: string; seamToken?: string; timeZone: string },
) => {
  // patterned string?
  let dateRange: DateRange | undefined = getDateFromPatternedString(dateString, timeZone);
  if (!dateRange) {
    if (dateString.includes(seamToken)) {
      // date Range
      const dates = dateString.split(seamToken);
      const from = dates[0];
      const to = dates[1];
      let parsedFrom = parseDateString(from, locale, timeZone, dateFormat);
      let parsedTo = parseDateString(to, locale, timeZone, dateFormat);

      if (isDayFormat(to.trim())) {
        parsedFrom = getZonedStartOfDay(parsedFrom, timeZone);
      }

      if (isDayFormat(to.trim())) {
        parsedTo = getZonedEndOfDay(parsedTo, timeZone);
      }

      dateRange = [parsedFrom, parsedTo];
    } else {
      // singe date

      const parsedDate = parseDateString(dateString, locale, timeZone, dateFormat);
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
): DateRange | undefined => {
  const matchedKey = getMatchedPatternKey(dateString);
  const now = new Date();

  switch (matchedKey) {
    case 'second': {
      const seconds = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subSeconds(now, seconds), now];
    }
    case 'minute': {
      const miunutes = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subMinutes(now, miunutes), now];
    }
    case 'hour': {
      const hours = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subHours(now, hours), now];
    }
    case 'day': {
      const days = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subDays(now, days), now];
    }
    case 'week': {
      const weeks = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subWeeks(now, weeks), now];
    }
    case 'year': {
      const year = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subYears(now, year), now];
    }
    case 'month': {
      const month = parseInt(dateString.replace(/\D/g, ''), 10);
      return [subMonths(now, month), now];
    }
    case 'yesterday': {
      const yesterday = subDays(now, 1);
      return [getZonedStartOfDay(yesterday, timeZone), getZonedEndOfDay(yesterday, timeZone)];
    }
    case 'today': {
      return [getZonedStartOfDay(now, timeZone), now];
    }
    case 'lastMonth': {
      const lastMonth = subMonths(now, 1);
      return [getZonedStartOfMonth(lastMonth, timeZone), getZonedEndOfMonth(lastMonth, timeZone)];
    }
    case 'lastYear': {
      const lastYear = subYears(now, 1);
      return [getZonedStartOfYear(lastYear, timeZone), getZonedEndOfYear(lastYear, timeZone)];
    }
    case 'unixTimestampRange': {
      const pattern = /(\d{13})\s*[^\d]\s*(\d{13})/;
      const match = dateString.match(pattern)!;

      const startDate = new Date(parseInt(match[1]));
      const endDate = new Date(parseInt(match[2]));
      return [startDate, endDate];
    }
    default:
      return;
  }
};

export const isDayFormat = (dateString: string) => {
  const dayPatterns = [/^[A-Za-z]{3} \d{1,2}$/, /^\d{1,2}\/\d{1,2}$/, /^\d{1,2}. \d{1,2}$/];

  return dayPatterns.some((pattern) => pattern.test(dateString));
};

export const calcZonedDate = (date: Date, tz: string, fn: (date: Date) => Date) => {
  const inputZoned = utcToZonedTime(date, tz);
  // const fnZoned = options ? fn(inputZoned, options) : fn(inputZoned);
  const fnZoned = fn(inputZoned);
  return zonedTimeToUtc(fnZoned, tz);
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

export const parseWithTimeZone = (
  dateString: string,
  format: string,
  referenceDate: Date,
  timeZone: string,
  options: Parameters<typeof parse>['3'],
) => {
  const zonedDate = utcToZonedTime(referenceDate, timeZone);
  const parsedDate = parse(dateString, format, zonedDate, options);
  return zonedTimeToUtc(parsedDate, timeZone);
};
