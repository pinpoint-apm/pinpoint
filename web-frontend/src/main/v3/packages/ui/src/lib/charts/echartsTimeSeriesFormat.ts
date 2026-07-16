import { isValid, isThisYear, isToday } from 'date-fns';
import { formatInTimeZone } from 'date-fns-tz';
import {
  escapeHTMLEntities,
  formatNewLinedDateString,
  getTimezone,
} from '@pinpoint-fe/ui/src/utils';

// 사용자 지정 타임존 기준의 x축 라벨/tooltip 헤더 포맷터. 오늘이면 시:분:초, 올해면 월.일 + 시:분:초,
// 그 외에는 연.월.일 + 시:분:초를 두 줄('\n')로 반환한다. (Heatmap tooltip 과 OpenTelemetry 차트가 공유)
export const defaultTickFormatter = (value: number) => {
  const timezone = getTimezone();

  if (isToday(value)) {
    return formatInTimeZone(value, timezone, 'HH:mm:ss');
  }
  if (isThisYear(value)) {
    return `${formatInTimeZone(value, timezone, 'MM.dd')}\n${formatInTimeZone(value, timezone, 'HH:mm:ss')}`;
  }
  return `${formatInTimeZone(value, timezone, 'yyyy.MM.dd')}\n${formatInTimeZone(value, timezone, 'HH:mm:ss')}`;
};

// category x축(timestamp) 라벨을 2줄짜리 날짜/시간 문자열로 포맷한다.
export const formatCategoryDateLabel = (value: number | string) => {
  const ts = typeof value === 'string' ? Number(value) : value;
  const date = new Date(ts);
  if (isValid(date)) {
    return formatNewLinedDateString(date);
  }
  return String(value);
};

interface AxisTooltipOptions {
  // 날짜 헤더 포맷을 주입한다. 기본은 브라우저 로컬 타임존 기준(formatNewLinedDateString).
  // 사용자 지정 타임존을 반영해야 하는 차트는 여기서 timezone 기반 포맷터를 넘긴다.
  formatDate?: (axisValue: number) => string;
  // 값이 null 인 시리즈 처리. 'hide'(기본): 행을 숨긴다(미수집=선 끊김 차트용).
  // 'zero': 0 으로 표시한다(0 을 렌더링 이슈로 null 치환한 차트용).
  nullBehavior?: 'hide' | 'zero';
}

// trigger: 'axis' 툴팁의 공통 포맷터. 날짜 헤더 + 시리즈별 색상 스와치/값 행을 그린다.
// 값 포맷은 차트마다 다르므로 formatValue 로 주입받는다.
export const formatAxisTooltip = (
  // echarts 툴팁 params 는 라이브러리 타입이 느슨해 any 로 받고 내부에서 좁힌다.
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  params: any,
  formatValue: (value: number) => string,
  { formatDate, nullBehavior = 'hide' }: AxisTooltipOptions = {},
) => {
  if (!Array.isArray(params) || params.length === 0) return '';
  const axisValue = params[0].axisValue;
  const axisValueNum = typeof axisValue === 'number' ? axisValue : Number(axisValue);
  const dateStr = formatDate
    ? formatDate(axisValueNum)
    : isValid(new Date(axisValueNum))
      ? formatNewLinedDateString(axisValueNum).replace('\n', ' ')
      : String(axisValue);
  const rows = params
    .map(
      (param: {
        value?: number | [number, number] | null;
        seriesName?: string;
        color?: string;
      }) => {
        const rawValue = typeof param.value === 'number' ? param.value : param.value?.[1];
        const yValue = rawValue == null && nullBehavior === 'zero' ? 0 : rawValue;
        if (yValue == null) return null;
        return `<div style="display: flex; justify-content: space-between; gap: 12px; align-items: center;">
                  <div style="display: flex; gap: 5px; align-items: center;">
                    <div style="width: 10px; height: 10px; background: ${param.color};"></div>${escapeHTMLEntities(String(param.seriesName ?? ''))}
                  </div>
                  <div>${formatValue(yValue)}</div>
                </div>`;
      },
    )
    .filter(Boolean)
    .join('');
  return `<div>
            <div style="margin-bottom: 5px;"><strong>${escapeHTMLEntities(dateStr)}</strong></div>
            ${rows}
          </div>`;
};
