import { parse, isValid, differenceInDays } from 'date-fns';
import { SEARCH_PARAMETER_DATE_FORMAT } from '@pinpoint-fe/ui/src/constants';
import { format, getCurrentFormat } from './format';

export const getParsedDateRange = (
  dates: {
    from?: Date | string;
    to?: Date | string;
  } = {},
  validateDateRange = isValidDateRange(2),
) => {
  const defaultGap = 5 * 60 * 1000;
  const currentDate = new Date();
  let parsedFrom = dates?.from || new Date(currentDate.getTime() - defaultGap);
  let parsedTo = dates?.to || currentDate;

  if (typeof parsedFrom === 'string') {
    parsedFrom = parse(parsedFrom, SEARCH_PARAMETER_DATE_FORMAT, currentDate);
  }
  if (typeof parsedTo === 'string') {
    parsedTo = parse(parsedTo, SEARCH_PARAMETER_DATE_FORMAT, currentDate);
  }
  if (!validateDateRange({ from: parsedFrom, to: parsedTo })) {
    parsedFrom = new Date(currentDate.getTime() - defaultGap);
    parsedTo = currentDate;
  }

  return {
    from: parsedFrom,
    to: parsedTo,
  };
};

export const getFormattedDateRange = (
  dateRange: {
    from: Date;
    to: Date;
  },
  dateFormat = SEARCH_PARAMETER_DATE_FORMAT,
) => {
  return {
    from: format(dateRange.from, dateFormat),
    to: format(dateRange.to, dateFormat),
  };
};

export const getParsedDate = (date: string) => {
  const currentDate = new Date();
  const result = parse(date, SEARCH_PARAMETER_DATE_FORMAT, new Date());
  if (isValid(result)) {
    return result;
  }
  return currentDate;
};

export const getParsedDates = (from: string, to: string): [number, number] => {
  return [getParsedDate(from).getTime(), getParsedDate(to).getTime()];
};

type DateRangeType = { from: Date; to: Date };

export const isValidDateRange =
  (dateRangeDays: number) =>
  ({ from, to }: DateRangeType) => {
    return (
      isValid(from) &&
      isValid(to) &&
      from.getTime() < to.getTime() &&
      Math.abs(differenceInDays(from, to)) <= dateRangeDays
    );
  };

export const convertToTimeUnit = (milliseconds = 0) => {
  const seconds = Math.ceil(milliseconds / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);

  if (days >= 2) {
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

export const spilitDateStringByHour = (dateString: string) => {
  const delimiter = /(?<=\s)hh/i;
  const splitArray = dateString.split(delimiter);
  // return splitArray;
  const firstPart = splitArray[0].trim();
  const secondPart = dateString.slice(firstPart.length).trim();
  return [firstPart, secondPart];
};

export const formatNewLinedDateString = (date: Date | number) => {
  const [firstFormat, secondFormat] = spilitDateStringByHour(getCurrentFormat());

  return `${format(date, firstFormat)}\n${format(date, secondFormat)}`;
};

export const convertTimeStringToTime = (timeString: string) => {
  const timePattern = /^(\d+)([mhd])$/;
  const match = timeString.match(timePattern);
  const value = Number(match?.[1]);
  const unit = match?.[2];

  switch (unit) {
    case 'm':
      return value * 60 * 1000;
    case 'h':
      return value * 60 * 60 * 1000;
    case 'd':
      return value * 24 * 60 * 60 * 1000;
    default:
      throw new Error('Unknown time unit');
  }
};
