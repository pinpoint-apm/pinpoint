import { TimePatternKeys, TimePatterns } from '../..';
import { timePattern as enPatterns } from './en';
import { timePattern as koPatterns } from './ko';

export const SEAM_TOKEN = '-';

export const dateFormats = [
  'MMM do',
  'M/d',
  'MM/dd',
  'yyyy/MM/dd',
  'HH:mm',
  'hh:mm a',
  'HH:mm:ss',
  'hh:mm:ss a',
  'HH:mm:ss.SSS',
  'hh:mm:ss.SSS a',
  'HH:mm:ss SSS',
  'hh:mm:ss SSS a',
  'MMM do, HH:mm',
  'MMM do, hh:mm a',
  'MMM do, HH:mm:ss',
  'MMM do, hh:mm:ss a',
  'MMM do, HH:mm:ss.SSS',
  'MMM do, hh:mm:ss.SSS a',
  'yyyy MMM do',
  'MMM do yyyy',
  'yyyy MMM do, HH:mm',
  'yyyy MMM do, hh:mm a',
  'yyyy MMM do, HH:mm:ss',
  'yyyy MMM do, hh:mm:ss a',
  'yyyy MMM do, HH:mm:ss.SSS',
  'yyyy MMM do, hh:mm:ss.SSS a',
  'MMM do yyyy, HH:mm',
  'MMM do yyyy, hh:mm a',
  'MMM do yyyy, HH:mm:ss',
  'MMM do yyyy, hh:mm:ss a',
  'MMM do yyyy, HH:mm:ss.SSS',
  'MMM do yyyy, hh:mm:ss.SSS a',
];

export const timePatternKeys: TimePatternKeys[] = [
  'second',
  'minute',
  'hour',
  'day',
  'week',
  'month',
  'year',
  'yesterday',
  'today',
  'lastMonth',
  'lastYear',
  'unixTimestampRange',
];

export const timePatterns = timePatternKeys.reduce((acc, key) => {
  return {
    ...acc,
    [key]: [enPatterns[key], koPatterns[key]],
  };
}, {} as TimePatterns);
