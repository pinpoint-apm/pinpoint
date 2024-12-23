import { getParsedDate, getParsedDateRange } from '@pinpoint-fe/ui/utils';

export const getSearchParameters = (search: string) => {
  return Object.fromEntries(
    Array.from(new URLSearchParams(search)).map(([key, value]) => [key, decodeURIComponent(value)]),
  );
};

export const getDateRange = (search: string, isRealtime: boolean): { from: Date; to: Date } => {
  const searchParameters = getSearchParameters(search);
  let newFrom: Date;
  let newTo: Date;
  if (isRealtime) {
    const newDate = getParsedDateRange();
    newFrom = newDate.from;
    newTo = newDate.to;
  } else {
    newFrom = getParsedDate(searchParameters.from);
    newTo = getParsedDate(searchParameters.to);
  }

  return { from: newFrom, to: newTo };
};
