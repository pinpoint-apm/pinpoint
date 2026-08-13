import { subMinutes } from 'date-fns';
import { getParsedDate } from '@pinpoint-fe/ui/src/utils';

/**
 * 실시간 화면이 보는 창의 폭. 실시간 경로에는 from/to가 없어 화면이 창을 직접 만든다.
 *
 * 이 값이 진짜 폭이므로 `getParsedDateRange()`의 기본 기간(20분)을 쓰면 안 된다. 첫 렌더가 20분
 * 창을 잡으면 그 폭으로 조회가 한 번 나가고, 곧바로 5분 창으로 다시 조회가 나간다.
 */
export const REALTIME_WINDOW_MINUTES = 5;

/** "지금부터 {@link REALTIME_WINDOW_MINUTES}분 전"까지의 창. */
export const getRealtimeDateRange = (): { from: Date; to: Date } => {
  const to = new Date();

  return { from: subMinutes(to, REALTIME_WINDOW_MINUTES), to };
};

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
    const newDate = getRealtimeDateRange();
    newFrom = newDate.from;
    newTo = newDate.to;
  } else {
    newFrom = getParsedDate(searchParameters.from);
    newTo = getParsedDate(searchParameters.to);
  }

  return { from: newFrom, to: newTo };
};
