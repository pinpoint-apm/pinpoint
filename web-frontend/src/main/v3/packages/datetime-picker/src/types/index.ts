export type TimeUnit = 's' | 'm' | 'h' | 'd' | 'w' | 'mo' | 'y';
export type TimeUnitFormat = `${number}${TimeUnit}` | 'today';
export type LocaleKey = 'en' | 'ko' | 'ja' | 'zh';
export type DateRange = [Date | null, Date | null];
export type TimePatternKeys =
  | 'second'
  | 'minute'
  | 'hour'
  | 'day'
  | 'week'
  | 'month'
  | 'year'
  | 'yesterday'
  | 'today'
  | 'lastMonth'
  | 'lastYear'
  | 'unixTimestampRange';
export type TimePattern = { [key in TimePatternKeys]: RegExp };
export type TimePatterns = { [key in TimePatternKeys]: RegExp[] };
