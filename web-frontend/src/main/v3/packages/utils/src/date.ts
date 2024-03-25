import { parse, isValid, differenceInDays, format } from 'date-fns';
import { DATE_FORMAT } from '@pinpoint-fe/constants';

type DateRangeType = { from: Date; to: Date };

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
    parsedFrom = parse(parsedFrom, DATE_FORMAT, currentDate);
  }
  if (typeof parsedTo === 'string') {
    parsedTo = parse(parsedTo, DATE_FORMAT, currentDate);
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
  dateFormat = DATE_FORMAT,
) => {
  return {
    from: format(dateRange.from, dateFormat),
    to: format(dateRange.to, dateFormat),
  };
};

export const getParsedDate = (date: string) => {
  const currentDate = new Date();
  const result = parse(date, DATE_FORMAT, new Date());
  if (isValid(result)) {
    return result;
  }
  return currentDate;
};

export const getParsedDates = (from: string, to: string): [number, number] => {
  return [getParsedDate(from).getTime(), getParsedDate(to).getTime()];
};

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
