import { parse, isValid, differenceInDays } from 'date-fns';
import { DateRange, DateRangeTime, DATE_FORMAT } from '@pinpoint-fe/constants';

type DateRangeType = { from: Date, to: Date };

export const getParsedDateRange = (dates: {
  from?: Date | string;
  to?: Date | string;
}, range = DateRange.FIVE_MINUTES) => {
  const currentDate = new Date();
  let parsedFrom = dates?.from || new Date(currentDate.getTime() - DateRangeTime[range]);
  let parsedTo = dates?.to || currentDate;

  if (typeof parsedFrom === 'string') {
    parsedFrom = parse(parsedFrom, DATE_FORMAT, currentDate);
  }
  if (typeof parsedTo === 'string') {
    parsedTo = parse(parsedTo, DATE_FORMAT, currentDate);
  }
  if (!isValidDateRangeInTwoDays({ from: parsedFrom, to: parsedTo})) {
    parsedFrom = new Date(currentDate.getTime() - DateRangeTime[range]);
    parsedTo = currentDate;
  }

  return {
    from: parsedFrom,
    to: parsedTo,
  }
}

export const isValidDateRange = (
  {from, to}: DateRangeType, 
  optionalCondition = true,
) => {
  return (
    isValid(from) 
    && isValid(to) 
    && (from.getTime() < to.getTime())
    && optionalCondition
  );
}

export const isValidDateRangeInTwoDays = (dateRange: Parameters<typeof isValidDateRange>[0]) => {
  return isValidDateRange(dateRange, Math.abs(differenceInDays(dateRange.from, dateRange.to)) <= 2);
}