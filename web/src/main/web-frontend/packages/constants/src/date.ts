const SECOND = 1000
const MINUTE = SECOND * 60;
const HOUR = MINUTE * 60;
const DAY = HOUR * 24;

export const DATE_FORMAT = 'yyyy-MM-dd-HH-mm-ss';

export const DateRange = {
  FIVE_MINUTES: '5m',
  TWENTY_MINUTES: '20m',
  ONE_HOUR: '1h',
  THREE_HOURS: '3h',
  SIX_HOURS: '6h',
  TWELVE_HOURS: '12h',
  ONE_DAY: '1d',
  TWO_DAYS: '2d',
  REAL_TIME: 'realtime',
} as const;

export const DateRangeTime: {[key: string]: number} = {
  [DateRange.FIVE_MINUTES]: MINUTE * 5,
  [DateRange.TWENTY_MINUTES]: MINUTE * 20,
  [DateRange.ONE_HOUR]: HOUR,
  [DateRange.THREE_HOURS]: HOUR * 3,
  [DateRange.SIX_HOURS]: HOUR * 6,
  [DateRange.TWELVE_HOURS]: HOUR * 12,
  [DateRange.ONE_DAY]: DAY,
  [DateRange.TWO_DAYS]: DAY * 2,
} as const;