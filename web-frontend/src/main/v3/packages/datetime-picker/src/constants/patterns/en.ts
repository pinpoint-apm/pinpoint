import { TimePattern } from '../../index';

export const timePattern: TimePattern = {
  second: /^(past)?\s*(\d+)\s*(s|sec|second|seconds)$/i,
  minute: /^(past)?\s*(\d+)\s*(m|min|minute|minutes)$/i,
  hour: /^(past)?\s*(\d+)\s*(h|hour|hours)$/i,
  day: /^(past)?\s*(\d+)\s*(d|day|days)$/i,
  week: /^(past)?\s*(\d+)\s*(w|week|weeks)$/i,
  month: /^(past)?\s*(\d+)\s*(mo|month|months)$/i,
  year: /^(past)?\s*(\d+)\s*(y|year|years)$/i,
  yesterday: /^yesterday$/i,
  today: /^today$/i,
  lastMonth: /^last\s*month$/i,
  lastYear: /^last\s*year$/i,
  unixTimestampRange: /^\d{13}\s*[^\d]\s*\d{13}$/,
};
