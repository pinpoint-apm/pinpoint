import { TimePattern } from '../../types';

export const timePattern: TimePattern = {
  second: /^(past|last)?\s*(\d+)\s*(s|sec|second|seconds)$/i,
  minute: /^(past|last)?\s*(\d+)\s*(m|min|minute|minutes)$/i,
  hour: /^(past|last)?\s*(\d+)\s*(h|hour|hours)$/i,
  day: /^(past|last)?\s*(\d+)\s*(d|day|days)$/i,
  week: /^(past|last)?\s*(\d+)\s*(w|week|weeks)$/i,
  month: /^(past|last)?\s*(\d+)\s*(mo|month|months)$/i,
  year: /^(past|last)?\s*(\d+)\s*(y|year|years)$/i,
  yesterday: /^yesterday$/i,
  today: /^today$/i,
  lastMonth: /^last\s*month$/i,
  lastYear: /^last\s*year$/i,
  unixTimestampRange: /^\d{13}\s*[^\d]\s*\d{13}$/,
};
