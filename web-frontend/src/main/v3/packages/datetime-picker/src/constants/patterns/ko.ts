import { TimePattern } from '../../index';

export const timePattern: TimePattern = {
  second: /^(\d+)\s*(초)(\s*전)?$/i,
  minute: /^(\d+)\s*(분)(\s*전)?$/i,
  hour: /^(\d+)\s*(시간)(\s*전)?$/i,
  day: /^(\d+)\s*(일)(\s*전)?$/i,
  week: /^(\d+)\s*(주)(\s*전)?$/i,
  month: /^(\d+)\s*(달|월|개월)(\s*전)?$/i,
  year: /^(\d+)\s*(년)(\s*전)?$/i,
  yesterday: /^어제$/i,
  today: /^오늘$/i,
  lastMonth: /^지난\/s*달$/i,
  lastYear: /^작\/s*$/i,
  unixTimestampRange: /^\d{13}\s*-\s*\d{13}$/,
};
