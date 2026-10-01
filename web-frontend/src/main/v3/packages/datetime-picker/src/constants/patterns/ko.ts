import { TimePattern } from '../../types';

export const timePattern: TimePattern = {
  second: /^(최근|지난)?\s*(\d+)\s*(초)(\s*전)?$/i,
  minute: /^(최근|지난)?\s*(\d+)\s*(분)(\s*전)?$/i,
  hour: /^(최근|지난)?\s*(\d+)\s*(시간)(\s*전)?$/i,
  day: /^(최근|지난)?\s*(\d+)\s*(일)(\s*전)?$/i,
  week: /^(최근|지난)?\s*(\d+)\s*(주)(\s*전)?$/i,
  month: /^(최근|지난)?\s*(\d+)\s*(달|월|개월)(\s*전)?$/i,
  year: /^(최근|지난)?\s*(\d+)\s*(년)(\s*전)?$/i,
  yesterday: /^어제$/i,
  today: /^오늘$/i,
  lastMonth: /^지난\s*달$/i,
  lastYear: /^(작년|지난\s*해)$/i,
  unixTimestampRange: /^\d{13}\s*-\s*\d{13}$/,
};
