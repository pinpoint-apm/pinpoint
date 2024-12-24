export const SEARCH_PARAMETER_DATE_FORMAT = 'yyyy-MM-dd-HH-mm-ss';
export const SEARCH_PARAMETER_DATE_FORMAT_WHITE_LIST = [
  SEARCH_PARAMETER_DATE_FORMAT,
  'yyyy-MM-dd-HH:mm:ss',
];

export const MAX_DATE_RANGE = {
  INSPECTOR: 1209600000, // 14day
} as const;

export enum DATE_FORMATS {
  'yyyy.MM.dd HH:mm:ss',
  'yyyy.MM.dd hh:mm:ss aa',
  'MMM do, yyyy HH:mm:ss',
  'MMM do, yyyy hh:mm:ss aa',
  'do MMM yyyy HH:mm:ss',
  'do MMM yyyy hh:mm:ss aa',
}
